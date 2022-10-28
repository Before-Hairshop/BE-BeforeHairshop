package com.beforehairshop.demo.recommend.service;

import com.amazonaws.services.ecs.model.StabilityStatus;
import com.amazonaws.services.xray.model.Http;
import com.beforehairshop.demo.aws.handler.CloudFrontUrlHandler;
import com.beforehairshop.demo.aws.service.AmazonS3Service;
import com.beforehairshop.demo.constant.member.StatusKind;
import com.beforehairshop.demo.fcm.service.FCMService;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.hairdesigner.handler.PageOffsetHandler;
import com.beforehairshop.demo.hairdesigner.repository.HairDesignerProfileRepository;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfile;
import com.beforehairshop.demo.member.repository.MemberProfileRepository;
import com.beforehairshop.demo.member.repository.MemberRepository;
import com.beforehairshop.demo.recommend.domain.Recommend;
import com.beforehairshop.demo.recommend.domain.RecommendImage;
import com.beforehairshop.demo.recommend.domain.RecommendRequest;
import com.beforehairshop.demo.recommend.dto.RecommendDto;
import com.beforehairshop.demo.recommend.dto.RecommendImageDto;
import com.beforehairshop.demo.recommend.dto.patch.RecommendPatchRequestDto;
import com.beforehairshop.demo.recommend.dto.post.RecommendSaveRequestDto;
import com.beforehairshop.demo.recommend.dto.response.RecommendDetailImageResponseDto;
import com.beforehairshop.demo.recommend.dto.response.RecommendDetailResponseDto;
import com.beforehairshop.demo.recommend.repository.RecommendImageRepository;
import com.beforehairshop.demo.recommend.repository.RecommendRepository;
import com.beforehairshop.demo.recommend.repository.RecommendRequestRepository;
import com.beforehairshop.demo.response.ResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.beforehairshop.demo.response.ResultDto.makeResult;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendService {

    private final CloudFrontUrlHandler cloudFrontUrlHandler;

    private final MemberRepository memberRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final HairDesignerProfileRepository hairDesignerProfileRepository;
    private final RecommendRepository recommendRepository;
    private final RecommendImageRepository recommendImageRepository;
    private final RecommendRequestRepository recommendRequestRepository;
    private final FCMService fcmService;

    @Transactional
    public ResponseEntity<ResultDto> save(Member recommender, BigInteger memberProfileId, RecommendSaveRequestDto recommendSaveRequestDto) {
        if (recommender == null) {
            log.error("[POST] /api/v1/recommend - 504 (세션 만료)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "당신의 세션이 만료되었습니다.");
        }
        MemberProfile memberProfile = memberProfileRepository.findById(memberProfileId).orElse(null);
        if (memberProfile == null) {
            log.error("[POST] /api/v1/recommend - 400 (추천을 받을 유저의 프로필이 없다)");
            return makeResult(HttpStatus.BAD_REQUEST, "추천을 받을 유저의 프로필이 없습니다.");
        }
        if (memberProfile.getMatchingActivationFlag() != 1) {
            log.error("[POST] /api/v1/recommend - 503 (추천서를 받을 유저가 매칭을 비활성화했다)");
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "추천서를 받을 유저가 매칭을 비활성화시켰습니다.");
        }

        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(recommender, StatusKind.NORMAL.getId()).orElse(null);
        if (hairDesignerProfile == null) {
            log.error("[POST] /api/v1/recommend - 404 (추천서를 보낼 디자이너의 프로필이 없다)");
            return makeResult(HttpStatus.NOT_FOUND, "추천서를 보낼 디자이너의 프로필이 없습니다");
        }
        Recommend recommend = new Recommend(hairDesignerProfile, memberProfile, recommendSaveRequestDto, StatusKind.NORMAL.getId());
        memberProfile.getRecommendedSet().add(recommend);
        hairDesignerProfile.getRecommendSet().add(recommend);

        RecommendRequest recommendRequest = recommendRequestRepository.findByToRecommendRequestProfileAndFromRecommendRequestProfileAndStatus(
                hairDesignerProfile, memberProfile, StatusKind.NORMAL.getId()
        ).orElse(null);

        if (recommendRequest != null) {
            memberProfile.getFromRecommendRequestSet().remove(recommendRequest);
            hairDesignerProfile.getToRecommendRequestSet().remove(recommendRequest);
            recommendRequestRepository.delete(recommendRequest);
        }

        // FCM push notification

        sendFCMMessageToMemberBySavingRecommend(memberProfile.getMember().getDeviceToken()
                , memberProfile.getMember().getId()
                , hairDesignerProfile.getName());

        return makeResult(HttpStatus.OK, new RecommendDto(recommend));
    }

    private void sendFCMMessageToMemberBySavingRecommend(String memberDeviceToken, BigInteger memberId, String designerName) {
        try {
            fcmService.sendMessageTo(memberDeviceToken, "비포헤어샵", designerName + " 디자이너 님의 스타일 추천서가 도착했으니 확인해보세요.");
        }
        catch (IOException exception) {
            log.error("[POST] /api/v1/recommend - FCM push notification fail (member id : " + memberId + ")");
            log.error(exception.getStackTrace().toString());
        }
    }

    @Transactional
    public ResponseEntity<ResultDto> saveImage(Member member, BigInteger recommendId, Integer imageCount, AmazonS3Service amazonS3Service) {
        if (member == null) {
            log.error("[POST] /api/v1/recommend/image - 504 (세션 만료)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "사용자 세션이 만료되었습니다.");
        }
        Recommend recommend = recommendRepository.findByIdAndStatus(recommendId, StatusKind.NORMAL.getId()).orElse(null);

        if (recommend == null) {
            log.error("[POST] /api/v1/recommend/image - 400 (잘못된 추천서 ID)");
            return makeResult(HttpStatus.BAD_REQUEST, "Recommend Id 가 잘못된 값입니다.");
        }

        List<String> recommendImagePreSignedUrlList = new ArrayList<>();
        for (int i = 0; i < imageCount; i++) {
           // RecommendImage recommendImage = new RecommendImage(recommend, null, StatusKind.NORMAL.getId());

            RecommendImage recommendImage = recommendImageRepository.save(
                    RecommendImage.builder()
                            .recommend(recommend)
                            .imageUrl(null)
                            .status(StatusKind.NORMAL.getId())
                            .build()
            );

            recommendImagePreSignedUrlList.add(
                    amazonS3Service.generatePreSignedUrl(
                            cloudFrontUrlHandler.getRecommendImageS3Path(recommendId, recommendImage.getId())
                    )
            );

            recommendImage.setImageUrl(
                    cloudFrontUrlHandler.getRecommendImageUrl(recommendId, recommendImage.getId())
            );
            recommend.getRecommendImageSet().add(recommendImage);
        }

        return makeResult(HttpStatus.OK, recommendImagePreSignedUrlList);
    }

    @Transactional
    public ResponseEntity<ResultDto> patch(Member member, BigInteger recommendId, RecommendPatchRequestDto patchDto) {
        if (member == null) {
            log.error("[PATCH] /api/v1/recommend - 504 (세션 만료)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션이 만료되었습니다.");
        }
        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(
                member, StatusKind.NORMAL.getId()
        ).orElse(null);

        Recommend recommend = recommendRepository.findByIdAndStatus(recommendId, StatusKind.NORMAL.getId()).orElse(null);
        if (hairDesignerProfile == null) {
            log.error("[PATCH] /api/v1/recommend - 404 (해당 ID를 가지는 추천서가 없다)");
            return makeResult(HttpStatus.NOT_FOUND, "해당 ID를 가지는 추천서가 없습니다.");
        }
        if (recommend == null || !recommend.getRecommenderProfile().getId().equals(hairDesignerProfile.getId())) {
            log.error("[PATCH] /api/v1/recommend - 400 (해당 유저는 해당 추천서를 수정할 권한이 없다)");
            return makeResult(HttpStatus.BAD_REQUEST, "해당 유저는 해당 추천서를 수정할 권한이 없습니다.");
        }

        // Entity 의 필드 값 수정
        recommend.patchEntity(patchDto);

        return makeResult(HttpStatus.OK, new RecommendDto(recommend));
    }

    @Transactional
    public ResponseEntity<ResultDto> patchImage(Member designer, BigInteger recommendId, Integer addImageCount, String[] deleteImageUrl, AmazonS3Service amazonS3Service) {
        if (designer == null) {
            log.error("[PATCH] /api/v1/recommend/image - 504 (세션 만료)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }
        Recommend recommend = recommendRepository.findByIdAndStatus(recommendId, StatusKind.NORMAL.getId()).orElse(null);
        if (recommend == null) {
            log.error("[PATCH] /api/v1/recommend/image - 400 (해당 ID를 가지는 추천서가 없다)");
            return makeResult(HttpStatus.BAD_REQUEST, "해당 ID를 가지는 스타일 추천서는 없습니다.");
        }
        for (int i = 0; i < deleteImageUrl.length; i++) {
            RecommendImage recommendImage = recommendImageRepository.findByImageUrlAndStatus(deleteImageUrl[i], StatusKind.NORMAL.getId()).orElse(null);
            if (recommendImage == null) {
                log.error("[PATCH] /api/v1/recommend/image - 404 (해당 URL 을 가지는 이미지는 없다. 삭제불가)");
                return makeResult(HttpStatus.NOT_FOUND, "해당 URL 을 가진 이미지는 없습니다.");
            }
            recommend.getRecommendImageSet().remove(recommendImage);

            // recommendImageRepository.delete(recommendImage);
        }

        List<String> addImagePreSignedUrlList = new ArrayList<>();
        for (int i = 0; i < addImageCount; i++) {
            RecommendImage recommendImage = recommendImageRepository.save(
                    RecommendImage.builder()
                            .recommend(recommend)
                            .imageUrl(null)
                            .status(StatusKind.NORMAL.getId())
                            .build()
            );

            addImagePreSignedUrlList.add(
                    amazonS3Service.generatePreSignedUrl(
                            cloudFrontUrlHandler.getRecommendImageS3Path(recommendId, recommendImage.getId())
                    )
            );

            recommendImage.setImageUrl(
                    cloudFrontUrlHandler.getRecommendImageUrl(recommendId, recommendImage.getId())
            );
            recommend.getRecommendImageSet().add(recommendImage);
        }

        return makeResult(HttpStatus.OK, addImagePreSignedUrlList);
    }

    @Transactional
    public ResponseEntity<ResultDto> acceptRecommend(Member member, BigInteger recommendId) {
        if (member == null) {
            log.error("[PATCH] /api/v1/recommend/response/accept - 504 (세션 만료)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }
        Recommend recommend = recommendRepository.findByIdAndStatus(recommendId, StatusKind.NORMAL.getId()).orElse(null);
        if (recommend == null) {
            log.error("[PATCH] /api/v1/recommend/response/accept - 400 (잘못된 추천서 ID)");
            return makeResult(HttpStatus.BAD_REQUEST, "잘못된 추천서 ID 입니다.");
        }
        recommend.acceptRecommend();

        sendFCMMessageToDesignerByAcceptRecommend(recommend.getRecommenderProfile().getHairDesigner().getDeviceToken()
                , member.getName()
                , recommend.getRecommenderProfile().getHairDesigner().getId());

        return makeResult(HttpStatus.OK, new RecommendDto(recommend));
    }

    private void sendFCMMessageToDesignerByAcceptRecommend(String designerDeviceToken, String memberName, BigInteger designerId) {
        try {
            fcmService.sendMessageTo(designerDeviceToken, "비포헤어샵",  memberName + " 님이 디자이너님이 제안하신 스타일 추천서를 수락하셨습니다!");
        }
        catch (IOException exception) {
            log.error("[PATCH] /api/v1/recommend/response/accept - FCM push notification fail (member id : " + designerId + ")");
            log.error(exception.getStackTrace().toString());
        }
    }

    @Transactional
    public ResponseEntity<ResultDto> rejectRecommend(Member member, BigInteger recommendId) {
        if (member == null) {
            log.error("[PATCH] /api/v1/recommend/response/reject - 504 (세션 만료)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }
        Recommend recommend = recommendRepository.findByIdAndStatus(recommendId, StatusKind.NORMAL.getId()).orElse(null);
        if (recommend == null) {
            log.error("[PATCH] /api/v1/recommend/response/reject - 400 (잘못된 추천서 ID)");
            return makeResult(HttpStatus.BAD_REQUEST, "잘못된 추천서 ID 입니다.");
        }

        recommend.rejectRecommend();

        return makeResult(HttpStatus.OK, new RecommendDto(recommend));
    }

    @Transactional
    public ResponseEntity<ResultDto> findOne(Member member, BigInteger recommendId) {
        if (member == null) {
            log.error("[GET] /api/v1/recommend/{recommend_id} - 504 (세션 만료)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }
        Recommend recommend = recommendRepository.findByIdAndStatus(recommendId, StatusKind.NORMAL.getId()).orElse(null);
        if (recommend == null) {
            log.error("[GET] /api/v1/recommend/{recommend_id} - 400 (잘못된 추천서 ID)");
            return makeResult(HttpStatus.BAD_REQUEST, "잘못된 추천서 ID 입니다.");
        }
        List<RecommendImageDto> recommendImageDtoList = new ArrayList<>();
        if (recommend.getRecommendImageSet() == null || recommend.getRecommendImageSet().size() == 0) {
            recommendImageDtoList = null;
        }
        else {
            recommendImageDtoList = recommend.getRecommendImageSet().stream()
                    .map(RecommendImageDto::new)
                    .collect(Collectors.toList());
        }

        return makeResult(HttpStatus.OK, new RecommendDetailImageResponseDto(
                recommend.getRecommenderProfile().getHairDesigner().getId()
                , recommend.getRecommenderProfile().getName()
                , recommend.getRecommenderProfile().getImageUrl()
                , recommend.getRecommendedProfile().getPhoneNumber()
                , new RecommendDto(recommend)
                ,recommendImageDtoList
        ));
    }


    @Transactional
    public ResponseEntity<ResultDto> findManyByMe(Member member) {
        if (member == null) {
            log.error("[GET] /api/v1/recommend/list_by_user - 504 (세션 만료)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }
        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null) {
            log.error("[GET] /api/v1/recommend/list_by_user - 404 (프로필이 등록되어 있지 않다)");
            return makeResult(HttpStatus.NOT_FOUND, "프로필이 등록되어 있지 않습니다.");
        }
        List<Recommend> recommendList = recommendRepository.findByRecommendedProfileAndStatusAndSortingByLocation(memberProfile.getId()
                , memberProfile.getLatitude(), memberProfile.getLongitude()
                , StatusKind.NORMAL.getId());

        if (recommendList == null)
            return makeResult(HttpStatus.OK, null);

        List<RecommendDetailResponseDto> recommendDetailResponseDtoList
                = recommendList.stream()
                .map(recommend -> new RecommendDetailResponseDto(
                        recommend.getRecommenderProfile().getHairDesigner().getId()
                        , recommend.getRecommenderProfile().getName()
                        , recommend.getRecommenderProfile().getImageUrl()
                        , new RecommendDto(recommend)
                )).collect(Collectors.toList());


        return makeResult(HttpStatus.OK, recommendDetailResponseDtoList);
    }

    @Transactional
    public ResponseEntity<ResultDto> delete(Member member, BigInteger recommendId) {
        if (member == null) {
            log.error("[DEL] /api/v1/recommend - 504 (세션 만료)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }
        Recommend recommend = recommendRepository.findByIdAndStatus(recommendId, StatusKind.NORMAL.getId()).orElse(null);
        if (recommend == null) {
            log.error("[DEL] /api/v1/recommend - 404 (잘못된 추천서 ID)");
            return makeResult(HttpStatus.NOT_FOUND, "해당 ID를 가지는 추천서는 없습니다");
        }
        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(
                member, StatusKind.NORMAL.getId()
        ).orElse(null);

        if (hairDesignerProfile == null) {
            log.error("[DEL] /api/v1/recommend - 400 (해당 디자이너의 프로필이 없다)");
            return makeResult(HttpStatus.BAD_REQUEST, "디자이너의 프로필이 없습니다.");
        }
        if (!recommend.getRecommenderProfile().getId().equals(hairDesignerProfile.getId())) {
            log.error("[DEL] /api/v1/recommend - 503 (해당 유저는 추천서를 삭제할 권한이 없다)");
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "해당 유저는 추천서를 삭제할 권한이 없습니다");
        }

        recommendRepository.delete(recommend);

        return makeResult(HttpStatus.OK, "삭제 완료");
    }

    @Transactional
    public ResponseEntity<ResultDto> findManyByDesigner(Member member) {
        if (member == null) {
            log.error("[GET] /api/v1/recommend/list_by_designer - 504 (세션 만료)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }
        if (member.getDesignerFlag() != 1 || !member.getRole().equals("ROLE_DESIGNER")) {
            log.error("[GET] /api/v1/recommend/list_by_designer - 400 (해당 유저는 디자이너가 아니다)");
            return makeResult(HttpStatus.BAD_REQUEST, "해당 유저가 디자이너가 아닙니다.");
        }


        HairDesignerProfile hairDesignerProfile
                = hairDesignerProfileRepository.findByHairDesignerAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (hairDesignerProfile == null) {
            log.error("[GET] /api/v1/recommend/list_by_designer - 404 (해당 디자이너는 프로필이 없다)");
            return makeResult(HttpStatus.NOT_FOUND, "해당 유저의 헤어 디자이너 프로필이 없습니다.");
        }
        List<Recommend> recommendList
                = recommendRepository.findByRecommenderProfileAndStatusOrderByCreateDate(hairDesignerProfile, StatusKind.NORMAL.getId());

        if (recommendList == null)
            return makeResult(HttpStatus.OK, null);

        List<RecommendDetailResponseDto> recommendDetailResponseDtoList = recommendList.stream()
                .map(recommend -> new RecommendDetailResponseDto(recommend.getRecommenderProfile().getHairDesigner().getId()
                        , recommend.getRecommenderProfile().getName()
                        , recommend.getRecommenderProfile().getImageUrl()
                        , new RecommendDto(recommend))).collect(Collectors.toList());

        return makeResult(HttpStatus.OK, recommendDetailResponseDtoList);

    }
}
