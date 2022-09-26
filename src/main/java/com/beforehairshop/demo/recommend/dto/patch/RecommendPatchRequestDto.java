package com.beforehairshop.demo.recommend.dto.patch;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendPatchRequestDto {
    private String greeting;
    private List<StyleRecommendPatchRequestDto> styleRecommendPatchRequestDtoList;
}
