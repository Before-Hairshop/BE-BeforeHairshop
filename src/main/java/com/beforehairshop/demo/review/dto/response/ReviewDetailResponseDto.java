package com.beforehairshop.demo.review.dto.response;

import com.beforehairshop.demo.review.domain.Review;
import com.beforehairshop.demo.review.domain.ReviewHashtag;
import com.beforehairshop.demo.review.domain.ReviewImage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDetailResponseDto {
    private Review review;
    private List<ReviewHashtag> hashtagList;
    private List<ReviewImage> imageList;
}
