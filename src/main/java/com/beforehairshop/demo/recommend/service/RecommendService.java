package com.beforehairshop.demo.recommend.service;

import com.beforehairshop.demo.aws.service.AmazonS3Service;
import com.beforehairshop.demo.constant.StatusKind;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfile;
import com.beforehairshop.demo.member.repository.MemberProfileRepository;
import com.beforehairshop.demo.recommend.domain.Recommend;
import com.beforehairshop.demo.recommend.domain.StyleRecommend;
import com.beforehairshop.demo.recommend.dto.post.RecommendSaveRequestDto;
import com.beforehairshop.demo.recommend.dto.response.RecommendDetailResponseDto;
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
import java.util.List;
import java.util.stream.Collectors;

import static com.beforehairshop.demo.response.ResultDto.makeResult;

@Service
@RequiredArgsConstructor
public class RecommendService {

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

        Recommend recommend = recommendRepository.save(recommendSaveRequestDto.toEntity(recommender, memberProfile.getMember()));

        List<StyleRecommend> styleRecommendList = styleRecommendRepository.saveAll(
                recommendSaveRequestDto.getStyleRecommendSaveRequestDtoList()
                    .stream()
                    .map(styleRecommendSaveRequestDto -> styleRecommendSaveRequestDto.toEntity(recommend))
                    .collect(Collectors.toList())
        );

        return makeResult(HttpStatus.OK, new RecommendDetailResponseDto(recommend, styleRecommendList, null));
    }

    @Transactional
    public ResponseEntity<ResultDto> saveImage(Member member, BigInteger styleRecommendId, Integer imageCount, AmazonS3Service amazonS3Service) {
        if (member == null)
            return makeResult(HttpStatus.BAD_REQUEST, "사용자 세션이 만료되었습니다.");

        StyleRecommend styleRecommend = styleRecommendRepository.findByIdAndStatus(styleRecommendId, StatusKind.NORMAL.getId()).orElse(null);

        if (styleRecommend == null)
            return makeResult(HttpStatus.BAD_REQUEST, "Style Recommend Id가 잘못된 값입니다.");


        for (int i = 0; i < imageCount; i++) {
            amazonS3Service.generatePreSignedUrl();
        }
    }
}
