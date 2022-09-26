package com.beforehairshop.demo.recommend.dto.response;

import com.beforehairshop.demo.recommend.domain.StyleRecommend;
import com.beforehairshop.demo.recommend.domain.StyleRecommendImage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StyleRecommendDetailResponseDto {
    private StyleRecommend styleRecommend;
    private List<StyleRecommendImage> styleRecommendImageList;
}
