package com.beforehairshop.demo.recommend.dto.post;

import com.beforehairshop.demo.constant.StatusKind;
import com.beforehairshop.demo.recommend.domain.Recommend;
import com.beforehairshop.demo.recommend.domain.StyleRecommend;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StyleRecommendSaveRequestDto {
    private String hairstyle;
    private String reason;
    private String price;

    public StyleRecommend toEntity(Recommend recommend) {
        return StyleRecommend.builder()
                .recommend(recommend)
                .hairstyle(hairstyle)
                .reason(reason)
                .price(price)
                .status(StatusKind.NORMAL.getId())
                .build();
    }
}
