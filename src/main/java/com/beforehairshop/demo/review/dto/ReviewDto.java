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
        this.id = review.getId();
        this.reviewerId = review.getReviewer().getId();
        this.hairDesignerId = review.getHairDesigner().getId();
        this.totalRating = review.getTotalRating();
        this.styleRating = review.getStyleRating();
        this.serviceRating = review.getServiceRating();
        this.content = review.getContent();
        this.createDate = review.getCreateDate();
        this.updateDate = review.getUpdateDate();
        this.status = review.getStatus();
    }
}
