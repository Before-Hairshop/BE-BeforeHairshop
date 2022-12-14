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
import com.google.firebase.messaging.FirebaseMessagingException;
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
    public ResponseEntity<ResultDto> save(Member recommender, BigInteger memberProfileId, RecommendSaveRequestDto recommendSaveRequestDto) throws FirebaseMessagingException, IOException {
        if (recommender == null) {
            log.error("[POST] /api/v1/recommend - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "????????? ????????? ?????????????????????.");
        }
        MemberProfile memberProfile = memberProfileRepository.findById(memberProfileId).orElse(null);
        if (memberProfile == null) {
            log.error("[POST] /api/v1/recommend - 400 (????????? ?????? ????????? ???????????? ??????)");
            return makeResult(HttpStatus.BAD_REQUEST, "????????? ?????? ????????? ???????????? ????????????.");
        }
        if (memberProfile.getMatchingActivationFlag() != 1) {
            log.error("[POST] /api/v1/recommend - 503 (???????????? ?????? ????????? ????????? ??????????????????)");
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "???????????? ?????? ????????? ????????? ???????????????????????????.");
        }

        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(recommender, StatusKind.NORMAL.getId()).orElse(null);
        if (hairDesignerProfile == null) {
            log.error("[POST] /api/v1/recommend - 404 (???????????? ?????? ??????????????? ???????????? ??????)");
            return makeResult(HttpStatus.NOT_FOUND, "???????????? ?????? ??????????????? ???????????? ????????????");
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

        try {
            sendFCMMessageToMemberBySavingRecommend(memberProfile.getMember().getDeviceToken()
                    , memberProfile.getMember().getId()
                    , hairDesignerProfile.getName());
        } catch (Exception e) {
            log.error("[POST] /api/v1/recommend - ???????????? ??????");
            return makeResult(HttpStatus.OK, new RecommendDto(recommend));
        }

        return makeResult(HttpStatus.OK, new RecommendDto(recommend));
    }

    private void sendFCMMessageToMemberBySavingRecommend(String memberDeviceToken, BigInteger memberId, String designerName) throws FirebaseMessagingException, IOException {
        fcmService.sendMessageTo(memberDeviceToken, "????????? ???????????? ??????????????????.", designerName + " ???????????? ?????? ????????? ???????????? ??????????????? ??????????????????.");

//        try {
//            fcmService.sendMessageTo(memberDeviceToken, "???????????????", designerName + " ???????????? ?????? ????????? ???????????? ??????????????? ??????????????????.");
//        }
//        catch (FirebaseMessagingException exception) {
//            log.error("[POST] /api/v1/recommend - FCM push notification fail (member id : " + memberId + ")");
//            log.error(exception.getStackTrace().toString());
//        }
    }

    @Transactional
    public ResponseEntity<ResultDto> saveImage(Member member, BigInteger recommendId, Integer imageCount, AmazonS3Service amazonS3Service) {
        if (member == null) {
            log.error("[POST] /api/v1/recommend/image - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "????????? ????????? ?????????????????????.");
        }
        Recommend recommend = recommendRepository.findByIdAndStatus(recommendId, StatusKind.NORMAL.getId()).orElse(null);

        if (recommend == null) {
            log.error("[POST] /api/v1/recommend/image - 400 (????????? ????????? ID)");
            return makeResult(HttpStatus.BAD_REQUEST, "Recommend Id ??? ????????? ????????????.");
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
            log.error("[PATCH] /api/v1/recommend - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "????????? ?????????????????????.");
        }
        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(
                member, StatusKind.NORMAL.getId()
        ).orElse(null);

        Recommend recommend = recommendRepository.findByIdAndStatus(recommendId, StatusKind.NORMAL.getId()).orElse(null);
        if (hairDesignerProfile == null) {
            log.error("[PATCH] /api/v1/recommend - 404 (?????? ID??? ????????? ???????????? ??????)");
            return makeResult(HttpStatus.NOT_FOUND, "?????? ID??? ????????? ???????????? ????????????.");
        }
        if (recommend == null || !recommend.getRecommenderProfile().getId().equals(hairDesignerProfile.getId())) {
            log.error("[PATCH] /api/v1/recommend - 400 (?????? ????????? ?????? ???????????? ????????? ????????? ??????)");
            return makeResult(HttpStatus.BAD_REQUEST, "?????? ????????? ?????? ???????????? ????????? ????????? ????????????.");
        }

        // Entity ??? ?????? ??? ??????
        recommend.patchEntity(patchDto);

        return makeResult(HttpStatus.OK, new RecommendDto(recommend));
    }

    @Transactional
    public ResponseEntity<ResultDto> patchImage(Member designer, BigInteger recommendId, Integer addImageCount, String[] deleteImageUrl, AmazonS3Service amazonS3Service) {
        if (designer == null) {
            log.error("[PATCH] /api/v1/recommend/image - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        Recommend recommend = recommendRepository.findByIdAndStatus(recommendId, StatusKind.NORMAL.getId()).orElse(null);
        if (recommend == null) {
            log.error("[PATCH] /api/v1/recommend/image - 400 (?????? ID??? ????????? ???????????? ??????)");
            return makeResult(HttpStatus.BAD_REQUEST, "?????? ID??? ????????? ????????? ???????????? ????????????.");
        }
        for (int i = 0; i < deleteImageUrl.length; i++) {
            RecommendImage recommendImage = recommendImageRepository.findByImageUrlAndStatus(deleteImageUrl[i], StatusKind.NORMAL.getId()).orElse(null);
            if (recommendImage == null) {
                log.error("[PATCH] /api/v1/recommend/image - 404 (?????? URL ??? ????????? ???????????? ??????. ????????????)");
                return makeResult(HttpStatus.NOT_FOUND, "?????? URL ??? ?????? ???????????? ????????????.");
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
    public ResponseEntity<ResultDto> acceptRecommend(Member member, BigInteger recommendId) throws FirebaseMessagingException, IOException {
        if (member == null) {
            log.error("[PATCH] /api/v1/recommend/response/accept - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        Recommend recommend = recommendRepository.findByIdAndStatus(recommendId, StatusKind.NORMAL.getId()).orElse(null);
        if (recommend == null) {
            log.error("[PATCH] /api/v1/recommend/response/accept - 400 (????????? ????????? ID)");
            return makeResult(HttpStatus.BAD_REQUEST, "????????? ????????? ID ?????????.");
        }
        recommend.acceptRecommend();

        try {
            sendFCMMessageToDesignerByAcceptRecommend(recommend.getRecommenderProfile().getHairDesigner().getDeviceToken()
                    , member.getName()
                    , recommend.getRecommenderProfile().getHairDesigner().getId());
        } catch (Exception e) {
            log.error("[PATCH] /api/v1/recommend/response/accept - ???????????? ??????");
            return makeResult(HttpStatus.OK, new RecommendDto(recommend));
        }
        return makeResult(HttpStatus.OK, new RecommendDto(recommend));
    }

    private void sendFCMMessageToDesignerByAcceptRecommend(String designerDeviceToken, String memberName, BigInteger designerId) throws FirebaseMessagingException, IOException {

        fcmService.sendMessageTo(designerDeviceToken, "????????? ???????????? ?????????????????????.",  memberName + " ?????? ???????????? ????????? ???????????? ?????????????????????.");
//      try {
//            fcmService.sendMessageTo(designerDeviceToken, "???????????????",  memberName + " ?????? ?????????????????? ???????????? ????????? ???????????? ?????????????????????!");
//        }
//        catch (FirebaseMessagingException exception) {
//            log.error("[PATCH] /api/v1/recommend/response/accept - FCM push notification fail (member id : " + designerId + ")");
//            log.error(exception.getStackTrace().toString());
//        }
    }

    @Transactional
    public ResponseEntity<ResultDto> rejectRecommend(Member member, BigInteger recommendId) {
        if (member == null) {
            log.error("[PATCH] /api/v1/recommend/response/reject - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        Recommend recommend = recommendRepository.findByIdAndStatus(recommendId, StatusKind.NORMAL.getId()).orElse(null);
        if (recommend == null) {
            log.error("[PATCH] /api/v1/recommend/response/reject - 400 (????????? ????????? ID)");
            return makeResult(HttpStatus.BAD_REQUEST, "????????? ????????? ID ?????????.");
        }

        recommend.rejectRecommend();

        return makeResult(HttpStatus.OK, new RecommendDto(recommend));
    }

    @Transactional
    public ResponseEntity<ResultDto> findOne(Member member, BigInteger recommendId) {
        if (member == null) {
            log.error("[GET] /api/v1/recommend/{recommend_id} - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        Recommend recommend = recommendRepository.findByIdAndStatus(recommendId, StatusKind.NORMAL.getId()).orElse(null);
        if (recommend == null) {
            log.error("[GET] /api/v1/recommend/{recommend_id} - 400 (????????? ????????? ID)");
            return makeResult(HttpStatus.BAD_REQUEST, "????????? ????????? ID ?????????.");
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
            log.error("[GET] /api/v1/recommend/list_by_user - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null) {
            log.error("[GET] /api/v1/recommend/list_by_user - 404 (???????????? ???????????? ?????? ??????)");
            return makeResult(HttpStatus.NOT_FOUND, "???????????? ???????????? ?????? ????????????.");
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
                        , recommend.getRecommenderProfile().getPhoneNumber()

                        , recommend.getRecommendedProfile().getName()
                        , recommend.getRecommendedProfile().getFrontImageUrl()
                        , recommend.getRecommendedProfile().getPhoneNumber()
                        , calculateDistance(memberProfile.getLatitude(), memberProfile.getLongitude(), recommend.getRecommenderProfile().getLatitude(), recommend.getRecommenderProfile().getLongitude())
                        , new RecommendDto(recommend)
                )).collect(Collectors.toList());


        return makeResult(HttpStatus.OK, recommendDetailResponseDtoList);
    }

    @Transactional
    public ResponseEntity<ResultDto> delete(Member member, BigInteger recommendId) {
        if (member == null) {
            log.error("[DEL] /api/v1/recommend - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        Recommend recommend = recommendRepository.findByIdAndStatus(recommendId, StatusKind.NORMAL.getId()).orElse(null);
        if (recommend == null) {
            log.error("[DEL] /api/v1/recommend - 404 (????????? ????????? ID)");
            return makeResult(HttpStatus.NOT_FOUND, "?????? ID??? ????????? ???????????? ????????????");
        }
        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(
                member, StatusKind.NORMAL.getId()
        ).orElse(null);

        if (hairDesignerProfile == null) {
            log.error("[DEL] /api/v1/recommend - 400 (?????? ??????????????? ???????????? ??????)");
            return makeResult(HttpStatus.BAD_REQUEST, "??????????????? ???????????? ????????????.");
        }
        if (!recommend.getRecommenderProfile().getId().equals(hairDesignerProfile.getId())) {
            log.error("[DEL] /api/v1/recommend - 503 (?????? ????????? ???????????? ????????? ????????? ??????)");
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "?????? ????????? ???????????? ????????? ????????? ????????????");
        }

        recommendRepository.delete(recommend);

        return makeResult(HttpStatus.OK, "?????? ??????");
    }

    @Transactional
    public ResponseEntity<ResultDto> findManyByDesigner(Member member) {
        if (member == null) {
            log.error("[GET] /api/v1/recommend/list_by_designer - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        if (member.getDesignerFlag() != 1 || !member.getRole().equals("ROLE_DESIGNER")) {
            log.error("[GET] /api/v1/recommend/list_by_designer - 400 (?????? ????????? ??????????????? ?????????)");
            return makeResult(HttpStatus.BAD_REQUEST, "?????? ????????? ??????????????? ????????????.");
        }


        HairDesignerProfile hairDesignerProfile
                = hairDesignerProfileRepository.findByHairDesignerAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (hairDesignerProfile == null) {
            log.error("[GET] /api/v1/recommend/list_by_designer - 404 (?????? ??????????????? ???????????? ??????)");
            return makeResult(HttpStatus.NOT_FOUND, "?????? ????????? ?????? ???????????? ???????????? ????????????.");
        }
        List<Recommend> recommendList
                = recommendRepository.findByRecommenderProfileAndStatusOrderByCreateDate(hairDesignerProfile, StatusKind.NORMAL.getId());

        if (recommendList == null)
            return makeResult(HttpStatus.OK, null);

        List<RecommendDetailResponseDto> recommendDetailResponseDtoList = recommendList.stream()
                .map(recommend -> new RecommendDetailResponseDto(
                        recommend.getRecommenderProfile().getHairDesigner().getId()
                        , recommend.getRecommenderProfile().getName()
                        , recommend.getRecommenderProfile().getImageUrl()
                        , recommend.getRecommenderProfile().getPhoneNumber()

                        , recommend.getRecommendedProfile().getName()
                        , recommend.getRecommendedProfile().getFrontImageUrl()
                        , recommend.getRecommendedProfile().getPhoneNumber()
                        , calculateDistance(hairDesignerProfile.getLatitude(), hairDesignerProfile.getLongitude(), recommend.getRecommendedProfile().getLatitude(), recommend.getRecommendedProfile().getLongitude())
                        , new RecommendDto(recommend))).collect(Collectors.toList());

        return makeResult(HttpStatus.OK, recommendDetailResponseDtoList);

    }

    private long calculateDistance(Float lat1, Float lon1, Float lat2, Float lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * 1609.344;

        return (Math.round(dist));
    }


    // This function converts decimal degrees to radians
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
}
