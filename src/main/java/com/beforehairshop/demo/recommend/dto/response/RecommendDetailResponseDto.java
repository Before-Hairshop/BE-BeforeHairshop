package com.beforehairshop.demo.recommend.dto.response;

import com.beforehairshop.demo.recommend.domain.Recommend;
import com.beforehairshop.demo.recommend.domain.StyleRecommend;
import com.beforehairshop.demo.recommend.domain.StyleRecommendImage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendDetailResponseDto {
    private Recommend recommend;
    private List<StyleRecommend> styleRecommendList;
    private List<StyleRecommendImage> styleRecommendImageList;
}
