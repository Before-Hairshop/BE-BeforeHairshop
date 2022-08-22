package com.beforehairshop.demo.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;


@Getter
@AllArgsConstructor
public class ReviewPatchRequestDto {
    private Integer totalRating;
    private Integer styleRating;
    private Integer serviceRating;
    private String content;
    private String virtualImageUrl;

    private List<ReviewHashtagPatchRequestDto> hashtagList;


}
