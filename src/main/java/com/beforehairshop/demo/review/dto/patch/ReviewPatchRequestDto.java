package com.beforehairshop.demo.review.dto.patch;

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

    private List<ReviewHashtagPatchRequestDto> hashtagList;


}
