package com.beforehairshop.demo.recommend.dto.response;

import com.beforehairshop.demo.recommend.domain.Recommend;
import com.beforehairshop.demo.recommend.domain.StyleRecommend;
import com.beforehairshop.demo.recommend.domain.StyleRecommendImage;
import com.beforehairshop.demo.recommend.dto.RecommendDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendDetailResponseDto {
    private RecommendDto recommendDto;
    private List<StyleRecommendDetailResponseDto> styleRecommendDetailResponseDtoList;
}
