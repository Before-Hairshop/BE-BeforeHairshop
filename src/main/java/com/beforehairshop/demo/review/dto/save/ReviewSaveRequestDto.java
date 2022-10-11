package com.beforehairshop.demo.review.dto.save;

import com.beforehairshop.demo.constant.member.StatusKind;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.review.domain.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigInteger;
import java.util.List;

@Getter
@AllArgsConstructor
public class ReviewSaveRequestDto {
    private BigInteger hairDesignerId;
    private Integer totalRating;
    private Integer styleRating;
    private Integer serviceRating;
    private String content;

    private List<ReviewHashtagSaveRequestDto> hashtagList;

    public Review toEntity(Member reviewer, HairDesignerProfile hairDesignerProfile) {
        return Review.builder()
                .reviewer(reviewer)
                .hairDesignerProfile(hairDesignerProfile)
                .totalRating(totalRating)
                .styleRating(styleRating)
                .serviceRating(serviceRating)
                .content(content)
                .status(StatusKind.NORMAL.getId())
                .build();
    }
}
