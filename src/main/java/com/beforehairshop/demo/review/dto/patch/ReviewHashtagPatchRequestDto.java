package com.beforehairshop.demo.review.dto.patch;

import com.beforehairshop.demo.constant.member.StatusKind;
import com.beforehairshop.demo.review.domain.Review;
import com.beforehairshop.demo.review.domain.ReviewHashtag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewHashtagPatchRequestDto {
    private String hashtag;

    public ReviewHashtag toEntity(Review review) {
        return ReviewHashtag.builder()
                .review(review)
                .hashtag(hashtag)
                .status(StatusKind.NORMAL.getId())
                .build();
    }
}
