package com.beforehairshop.demo.recommend.service;

import com.beforehairshop.demo.aws.handler.CloudFrontUrlHandler;
import com.beforehairshop.demo.aws.service.AmazonS3Service;
import com.beforehairshop.demo.constant.member.StatusKind;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.hairdesigner.handler.PageOffsetHandler;
import com.beforehairshop.demo.hairdesigner.repository.HairDesignerProfileRepository;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfile;
import com.beforehairshop.demo.member.repository.MemberProfileRepository;
import com.beforehairshop.demo.member.repository.MemberRepository;
import com.beforehairshop.demo.recommend.domain.Recommend;
import com.beforehairshop.demo.recommend.domain.RecommendImage;
import com.beforehairshop.demo.recommend.dto.RecommendDto;
import com.beforehairshop.demo.recommend.dto.patch.RecommendPatchRequestDto;
import com.beforehairshop.demo.recommend.dto.post.RecommendSaveRequestDto;
import com.beforehairshop.demo.recommend.repository.RecommendImageRepository;
import com.beforehairshop.demo.recommend.repository.RecommendRepository;
import com.beforehairshop.demo.response.ResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.beforehairshop.demo.response.ResultDto.makeResult;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final MemberRepository memberRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final HairDesignerProfileRepository hairDesignerProfileRepository;
    private final RecommendRepository recommendRepository;
    private final RecommendImageRepository recommendImageRepository;

    @Transactional
    public ResponseEntity<ResultDto> save(Member recommender, BigInteger memberProfileId, RecommendSaveRequestDto recommendSaveRequestDto) {
        if (recommender == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "당신의 세션이 만료되었습니다.");

        MemberProfile memberProfile = memberProfileRepository.findById(memberProfileId).orElse(null);
        if (memberProfile == null)
            return makeResult(HttpStatus.BAD_REQUEST, "추천을 받을 유저가 유효하지 않습니다.");

        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(recommender, StatusKind.NORMAL.getId()).orElse(null);
        if (hairDesignerProfile == null)
            return makeResult(HttpStatus.BAD_GATEWAY, "추천을 보낼 유저가 유효하지 않다.");

        Recommend recommend = new Recommend(hairDesignerProfile, memberProfile, recommendSaveRequestDto, StatusKind.NORMAL.getId());
        memberProfile.getRecommendedSet().add(recommend);
        hairDesignerProfile.getRecommendSet().add(recommend);

        Member updatedRecommender = memberRepository.findByIdAndStatus(recommender.getId(), StatusKind.NORMAL.getId()).orElse(null);
        Member updatedRecommendedPerson = memberRepository.findByIdAndStatus(memberProfile.getMember().getId(), StatusKind.NORMAL.getId()).orElse(null);
        if (updatedRecommender == null || updatedRecommendedPerson == null)
            return makeResult(HttpStatus.BAD_REQUEST, "추천하는 사람 혹은 추천받는 사람의 member entity 가 null 입니다.");

        return makeResult(HttpStatus.OK, new RecommendDto(recommend));
    }

    @Transactional
    public ResponseEntity<ResultDto> saveImage(Member member, BigInteger recommendId, Integer imageCount, AmazonS3Service amazonS3Service) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "사용자 세션이 만료되었습니다.");

        Recommend recommend = recommendRepository.findByIdAndStatus(recommendId, StatusKind.NORMAL.getId()).orElse(null);

        if (recommend == null)
            return makeResult(HttpStatus.BAD_REQUEST, "Style Recommend Id가 잘못된 값입니다.");


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
                        CloudFrontUrlHandler.getRecommendImageS3Path(recommendId, recommendImage.getId())
                    )
            );

            recommendImage.setImageUrl(
                    CloudFrontUrlHandler.getRecommendImageUrl(recommendId, recommendImage.getId())
            );
            recommend.getRecommendImageSet().add(recommendImage);
        }

        return makeResult(HttpStatus.OK, recommendImagePreSignedUrlList);
    }

    @Transactional
    public ResponseEntity<ResultDto> patch(Member member, BigInteger recommendId, RecommendPatchRequestDto patchDto) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션이 만료되었습니다.");

        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(
                member, StatusKind.NORMAL.getId()
        ).orElse(null);

        Recommend recommend = recommendRepository.findByIdAndStatus(recommendId, StatusKind.NORMAL.getId()).orElse(null);
        if (hairDesignerProfile == null || !recommend.getRecommenderProfile().getId().equals(hairDesignerProfile.getId()) ||recommend == null)
            return makeResult(HttpStatus.NOT_FOUND, "해당 ID를 가지는 추천서가 없거나 추천서를 수정할 권한이 없습니다.");

        // Entity 의 필드 값 수정
        recommend.patchEntity(patchDto);

        return makeResult(HttpStatus.OK, new RecommendDto(recommend));
    }

    @Transactional
    public ResponseEntity<ResultDto> patchImage(Member designer, BigInteger recommendId, Integer addImageCount, String[] deleteImageUrl, AmazonS3Service amazonS3Service) {
        if (designer == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");

        Recommend recommend = recommendRepository.findByIdAndStatus(recommendId, StatusKind.NORMAL.getId()).orElse(null);
        if (recommend == null)
            return makeResult(HttpStatus.BAD_REQUEST, "해당 ID를 가지는 스타일 추천서는 없습니다.");

        for (int i = 0; i < deleteImageUrl.length; i++) {
            RecommendImage recommendImage = recommendImageRepository.findByImageUrlAndStatus(deleteImageUrl[i], StatusKind.NORMAL.getId()).orElse(null);
            if (recommendImage == null)
                return makeResult(HttpStatus.NOT_FOUND, "해당 URL 을 가진 이미지는 없습니다.");

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
                            CloudFrontUrlHandler.getRecommendImageS3Path(recommendId, recommendImage.getId())
                    )
            );

            recommendImage.setImageUrl(
                    CloudFrontUrlHandler.getRecommendImageUrl(recommendId, recommendImage.getId())
            );
            recommend.getRecommendImageSet().add(recommendImage);
        }

        return makeResult(HttpStatus.OK, addImagePreSignedUrlList);
    }

    @Transactional
    public ResponseEntity<ResultDto> acceptRecommend(Member member, BigInteger recommendId) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");

        Recommend recommend = recommendRepository.findByIdAndStatus(recommendId, StatusKind.NORMAL.getId()).orElse(null);
        if (recommend == null)
            return makeResult(HttpStatus.BAD_REQUEST, "잘못된 추천서 ID 입니다.");

        recommend.acceptRecommend();

        return makeResult(HttpStatus.OK, new RecommendDto(recommend));
    }

    @Transactional
    public ResponseEntity<ResultDto> rejectRecommend(Member member, BigInteger recommendId) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");

        Recommend recommend = recommendRepository.findByIdAndStatus(recommendId, StatusKind.NORMAL.getId()).orElse(null);
        if (recommend == null)
            return makeResult(HttpStatus.BAD_REQUEST, "잘못된 추천서 ID 입니다.");

        recommend.rejectRecommend();

        return makeResult(HttpStatus.OK, new RecommendDto(recommend));
    }

    @Transactional
    public ResponseEntity<ResultDto> findOne(Member member, BigInteger recommendId) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");

        Recommend recommend = recommendRepository.findByIdAndStatus(recommendId, StatusKind.NORMAL.getId()).orElse(null);
        if (recommend == null)
            return makeResult(HttpStatus.BAD_REQUEST, "잘못된 추천서 ID 입니다.");

        return makeResult(HttpStatus.OK, new RecommendDto(recommend));
    }


    @Transactional
    public ResponseEntity<ResultDto> findManyByMe(Member member, Integer pageNumber) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");

        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null)
            return makeResult(HttpStatus.NOT_FOUND, "프로필이 등록되어 있지 않습니다.");

        // 위치 순서로 3km 이내의 헤어
        List<Recommend> recommendList = recommendRepository.findByRecommendedPersonAndStatusAndSortingByLocation(member.getId(), memberProfile.getLatitude(), memberProfile.getLongitude()
                , StatusKind.NORMAL.getId(), new PageOffsetHandler().getOffsetByPageNumber(pageNumber));

        List<RecommendDto> recommendDtoList = recommendList.stream()
                .map(RecommendDto::new)
                .collect(Collectors.toList());

        return makeResult(HttpStatus.OK, recommendDtoList);
    }

    @Transactional
    public ResponseEntity<ResultDto> delete(Member member, BigInteger recommendId) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");

        Recommend recommend = recommendRepository.findByIdAndStatus(recommendId, StatusKind.NORMAL.getId()).orElse(null);
        if (recommend == null)
            return makeResult(HttpStatus.NOT_FOUND, "해당 ID를 가지는 추천서는 없습니다");

        recommendRepository.delete(recommend);

        return makeResult(HttpStatus.OK, "삭제 완료");
    }
}
