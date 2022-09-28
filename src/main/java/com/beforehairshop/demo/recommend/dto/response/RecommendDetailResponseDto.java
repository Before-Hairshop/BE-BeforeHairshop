package com.beforehairshop.demo.recommend.dto.response;

import com.beforehairshop.demo.recommend.dto.RecommendDto;
import com.beforehairshop.demo.recommend.dto.RecommendImageDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendDetailResponseDto {
    private RecommendDto recommendDto;
    private List<RecommendImageDto> recommendImageDtoList;
}
