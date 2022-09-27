package com.beforehairshop.demo.recommend.dto;

import com.beforehairshop.demo.recommend.domain.StyleRecommend;
import com.beforehairshop.demo.recommend.domain.StyleRecommendImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Date;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StyleRecommendImageDto {
    private BigInteger id;
    private BigInteger styleRecommendId;
    private String imageUrl;
    private Date createDate;
    private Date updateDate;
    private int status;


    public StyleRecommendImageDto(StyleRecommendImage styleRecommendImage) {
        StyleRecommendImageDto.builder()
                .id(styleRecommendImage.getId())
                .styleRecommendId(styleRecommendImage.getStyleRecommend().getId())
                .imageUrl(styleRecommendImage.getImageUrl())
                .createDate(styleRecommendImage.getCreateDate())
                .updateDate(styleRecommendImage.getUpdateDate())
                .status(styleRecommendImage.getStatus())
                .build();
    }
}
