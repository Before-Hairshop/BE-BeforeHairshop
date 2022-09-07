package com.beforehairshop.demo.review.dto;

import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.review.domain.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigInteger;
import java.util.List;

@Getter
@AllArgsConstructor
public class ReviewSaveRequestDto {
    private BigInteger memberId;
    private BigInteger hairDesignerId;
    private Integer totalRating;
    private Integer styleRating;
    private Integer serviceRating;
    private String content;
    private String virtualImageUrl;

    private List<ReviewHashtagSaveRequestDto> hashtagList;

    public Review toEntity(Member member, Member hairDesigner) {
        return Review.builder()
                .member(member)
                .hairDesigner(hairDesigner)
                .totalRating(totalRating)
                .styleRating(styleRating)
                .serviceRating(serviceRating)
                .content(content)
                .virtualImageUrl(virtualImageUrl)
                .status(1)
                .build();
    }
}
