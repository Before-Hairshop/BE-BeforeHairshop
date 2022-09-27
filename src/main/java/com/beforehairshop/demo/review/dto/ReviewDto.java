package com.beforehairshop.demo.review.dto;

import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.review.domain.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Date;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private BigInteger id;
    private BigInteger reviewerId;
    private BigInteger hairDesignerId;
    private Integer totalRating;
    private Integer styleRating;
    private Integer serviceRating;
    private String content;
    private Date createDate;
    private Date updateDate;
    private int status;

    public ReviewDto(Review review) {
        ReviewDto.builder()
                .id(review.getId())
                .reviewerId(review.getReviewer().getId())
                .hairDesignerId(review.getHairDesigner().getId())
                .totalRating(review.getTotalRating())
                .styleRating(review.getStyleRating())
                .serviceRating(review.getServiceRating())
                .content(review.getContent())
                .createDate(review.getCreateDate())
                .updateDate(review.getUpdateDate())
                .status(review.getStatus())
                .build();
    }
}
