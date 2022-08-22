package com.beforehairshop.demo.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class ReviewPatchRequestDto {
    private Integer totalRating;
    private Integer styleRating;
    private Integer serviceRating;
    private String content;
    private String virtualImageUrl;



}
