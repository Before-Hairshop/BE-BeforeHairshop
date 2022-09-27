package com.beforehairshop.demo.recommend.dto.response;

import com.beforehairshop.demo.recommend.domain.StyleRecommend;
import com.beforehairshop.demo.recommend.domain.StyleRecommendImage;
import com.beforehairshop.demo.recommend.dto.StyleRecommendDto;
import com.beforehairshop.demo.recommend.dto.StyleRecommendImageDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StyleRecommendDetailResponseDto {
    private StyleRecommendDto styleRecommendDto;
    private List<StyleRecommendImageDto> styleRecommendImageDtoList;
}
