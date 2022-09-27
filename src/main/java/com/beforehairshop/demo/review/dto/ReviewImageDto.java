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
        ReviewImageDto.builder()
                .id(reviewImage.getId())
                .reviewId(reviewImage.getReview().getId())
                .imageUrl(reviewImage.getImageUrl())
                .createDate(reviewImage.getCreateDate())
                .updateDate(reviewImage.getUpdateDate())
                .status(reviewImage.getStatus())
                .build();
    }
}
