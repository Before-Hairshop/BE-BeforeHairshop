package com.beforehairshop.demo.review.dto;

import com.beforehairshop.demo.review.domain.Review;
import com.beforehairshop.demo.review.domain.ReviewHashtag;
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
public class ReviewHashtagDto {
    private BigInteger id;
    private BigInteger reviewId;
    private String hashtag;
    private Date createDate;
    private Date updateDate;
    private int status;

    public ReviewHashtagDto(ReviewHashtag reviewHashtag) {
        this.id = reviewHashtag.getId();
        this.reviewId =reviewHashtag.getReview().getId();
        this.hashtag = reviewHashtag.getHashtag();
        this.createDate = reviewHashtag.getCreateDate();
        this.updateDate = reviewHashtag.getUpdateDate();
        this.status = reviewHashtag.getStatus();
    }
}
