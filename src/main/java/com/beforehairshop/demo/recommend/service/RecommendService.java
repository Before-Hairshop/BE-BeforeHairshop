package com.beforehairshop.demo.recommend.service;

import com.amazonaws.services.mediatailor.model.HttpPackageConfiguration;
import com.beforehairshop.demo.auth.handler.PrincipalDetailsUpdater;
import com.beforehairshop.demo.aws.handler.CloudFrontUrlHandler;
import com.beforehairshop.demo.aws.service.AmazonS3Service;
import com.beforehairshop.demo.constant.StatusKind;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfile;
import com.beforehairshop.demo.member.repository.MemberProfileRepository;
import com.beforehairshop.demo.member.repository.MemberRepository;
import com.beforehairshop.demo.recommend.domain.Recommend;
import com.beforehairshop.demo.recommend.domain.StyleRecommend;
import com.beforehairshop.demo.recommend.domain.StyleRecommendImage;
import com.beforehairshop.demo.recommend.dto.patch.RecommendPatchRequestDto;
import com.beforehairshop.demo.recommend.dto.post.RecommendSaveRequestDto;
import com.beforehairshop.demo.recommend.dto.response.RecommendDetailResponseDto;
import com.beforehairshop.demo.recommend.dto.response.StyleRecommendDetailResponseDto;
import com.beforehairshop.demo.recommend.repository.RecommendRepository;
import com.beforehairshop.demo.recommend.repository.StyleRecommendImageRepository;
import com.beforehairshop.demo.recommend.repository.StyleRecommendRepository;
import com.beforehairshop.demo.response.ResultDto;
import lombok.RequiredArgsConstructor;
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
    private final RecommendRepository recommendRepository;
    private final StyleRecommendRepository styleRecommendRepository;
    private final StyleRecommendImageRepository styleRecommendImageRepository;

    @Transactional
    public ResponseEntity<ResultDto> save(Member recommender, BigInteger memberProfileId, RecommendSaveRequestDto recommendSaveRequestDto) {
        MemberProfile memberProfile = memberProfileRepository.findById(memberProfileId).orElse(null);
        if (memberProfile == null)
            return makeResult(HttpStatus.BAD_REQUEST, "추천을 보낼 유저가 유효하지 않습니다.");

        if (recommender == null)
            return makeResult(HttpStatus.BAD_REQUEST, "당신의 세션이 만료되었습니다.");

        Member updatedRecommender = memberRepository.findByIdAndStatus(recommender.getId(), StatusKind.NORMAL.getId()).orElse(null);
        Member recommendedPerson = memberRepository.findByIdAndStatus(memberProfile.getMember().getId(), StatusKind.NORMAL.getId()).orElse(null);
        if (updatedRecommender == null || recommendedPerson == null)
            return makeResult(HttpStatus.BAD_REQUEST, "추천하는 사람과 추천받는 사람의 member entity 가 null 입니다.");


        Recommend recommend = recommendRepository.save(recommendSaveRequestDto.toEntity(recommender, memberProfile.getMember()));

        List<StyleRecommend> styleRecommendList = styleRecommendRepository.saveAll(
                recommendSaveRequestDto.getStyleRecommendSaveRequestDtoList()
                    .stream()
                    .map(styleRecommendSaveRequestDto -> styleRecommendSaveRequestDto.toEntity(recommend))
                    .collect(Collectors.toList())
        );

        updatedRecommender.getRecommendSet().add(recommend);
        recommendedPerson.getRecommendedSet().add(recommend);

        PrincipalDetailsUpdater.setAuthenticationOfSecurityContext(updatedRecommender, "ROLE_DESIGNER");

        List<StyleRecommendDetailResponseDto> styleRecommendDetailResponseDtoList = new ArrayList<>();
        for (int i = 0; i < styleRecommendList.size(); i++) {
            styleRecommendDetailResponseDtoList.add(new StyleRecommendDetailResponseDto(
                    styleRecommendList.get(i), null
            ));
        }

        return makeResult(HttpStatus.OK, new RecommendDetailResponseDto(recommend, styleRecommendDetailResponseDtoList));
    }

    @Transactional
    public ResponseEntity<ResultDto> saveImage(Member member, BigInteger styleRecommendId, Integer imageCount, AmazonS3Service amazonS3Service) {
        if (member == null)
            return makeResult(HttpStatus.BAD_REQUEST, "사용자 세션이 만료되었습니다.");

        StyleRecommend styleRecommend = styleRecommendRepository.findByIdAndStatus(styleRecommendId, StatusKind.NORMAL.getId()).orElse(null);

        if (styleRecommend == null)
            return makeResult(HttpStatus.BAD_REQUEST, "Style Recommend Id가 잘못된 값입니다.");


        List<String> styleRecommendImagePreSignedUrlList = new ArrayList<>();
        for (int i = 0; i < imageCount; i++) {
            StyleRecommendImage styleRecommendImage = styleRecommendImageRepository.save(
                    StyleRecommendImage.builder()
                            .styleRecommend(styleRecommend)
                            .imageUrl(null)
                            .status(StatusKind.NORMAL.getId())
                            .build()
            );

            styleRecommendImagePreSignedUrlList.add(
                    amazonS3Service.generatePreSignedUrl(
                        CloudFrontUrlHandler.getStyleRecommendImageS3Path(styleRecommendId, styleRecommendImage.getId())
                    )
            );

            styleRecommendImage.setImageUrl(
                    CloudFrontUrlHandler.getStyleRecommendImageUrl(styleRecommendId, styleRecommendImage.getId())
            );
        }

        return makeResult(HttpStatus.OK, styleRecommendImagePreSignedUrlList);
    }

    @Transactional
    public ResponseEntity<ResultDto> patch(Member member, BigInteger recommendId, RecommendPatchRequestDto patchDto) {
        if (member == null)
            return makeResult(HttpStatus.BAD_REQUEST, "세션이 만료되었습니다.");

        Recommend recommend = recommendRepository.findByIdAndStatus(recommendId, StatusKind.NORMAL.getId()).orElse(null);
        if (recommend != null && patchDto.getGreeting() != null)
            recommend.setGreeting(patchDto.getGreeting());

        List<StyleRecommend> styleRecommendList = styleRecommendRepository.findByRecommendAndStatus(recommend, StatusKind.NORMAL.getId());


        for (int i = 0; i < patchDto.getStyleRecommendPatchRequestDtoList().size(); i++) {
            StyleRecommend styleRecommend = styleRecommendRepository.findByIdAndStatus(
                    patchDto.getStyleRecommendPatchRequestDtoList().get(i).getStyleRecommendId()
                    , StatusKind.NORMAL.getId()
            ).orElse(null);

            if (styleRecommend == null) return makeResult(HttpStatus.BAD_REQUEST, "잘못된 style recommend id 가 입력되었습니다.");

            // 수정 과정
            if (patchDto.getStyleRecommendPatchRequestDtoList().get(i).getHairstyle() != null)
                styleRecommend.setHairstyle(patchDto.getStyleRecommendPatchRequestDtoList().get(i).getHairstyle());

            if (patchDto.getStyleRecommendPatchRequestDtoList().get(i).getPrice() != null)
                styleRecommend.setPrice(patchDto.getStyleRecommendPatchRequestDtoList().get(i).getPrice());

            if (patchDto.getStyleRecommendPatchRequestDtoList().get(i).getReason() != null)
                styleRecommend.setReason(patchDto.getStyleRecommendPatchRequestDtoList().get(i).getReason());
        }

//        List<StyleRecommendDetailResponseDto> styleRecommendDetailResponseDtoList = new ArrayList<>();
//        for (int i = 0; i < styleRecommendList.size(); i++) {
//            styleRecommendDetailResponseDtoList.add(new StyleRecommendDetailResponseDto(
//                    styleRecommendList.get(i),
//                    styleRecommendImageRepository.findByStyleRecommendAndStatus(styleRecommendList.get(i), StatusKind.NORMAL.getId())
//            ));
//        }

        return makeResult(HttpStatus.OK, recommend);
    }

    @Transactional
    public ResponseEntity<ResultDto> patchImage(Member designer, BigInteger styleRecommendId, Integer addImageCount, String[] deleteImageUrl, AmazonS3Service amazonS3Service) {
        return null;
    }
}
