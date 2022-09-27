package com.beforehairshop.demo.review.dto.response;

import com.beforehairshop.demo.review.domain.Review;
import com.beforehairshop.demo.review.domain.ReviewHashtag;
import com.beforehairshop.demo.review.domain.ReviewImage;
import com.beforehairshop.demo.review.dto.ReviewDto;
import com.beforehairshop.demo.review.dto.ReviewHashtagDto;
import com.beforehairshop.demo.review.dto.ReviewImageDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDetailResponseDto {
    private ReviewDto reviewDto;
    private List<ReviewHashtagDto> hashtagDtoList;
    private List<ReviewImageDto> imageDtoList;
}
