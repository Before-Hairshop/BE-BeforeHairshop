package com.beforehairshop.demo.review.dto;

import com.beforehairshop.demo.review.domain.Review;
import com.beforehairshop.demo.review.domain.ReviewImage;
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
public class ReviewImageDto {
    private BigInteger id;
    private BigInteger reviewId;
    private String imageUrl;
    private Date createDate;
    private Date updateDate;
    private int status;

    public ReviewImageDto(ReviewImage reviewImage) {
        this.id = reviewImage.getId();
        this.reviewId = reviewImage.getReview().getId();
        this.imageUrl = reviewImage.getImageUrl();
        this.createDate = reviewImage.getCreateDate();
        this.updateDate = reviewImage.getUpdateDate();
        this.status = reviewImage.getStatus();
    }
}
