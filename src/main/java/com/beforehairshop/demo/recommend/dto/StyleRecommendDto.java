package com.beforehairshop.demo.recommend.dto;

import com.beforehairshop.demo.recommend.domain.Recommend;
import com.beforehairshop.demo.recommend.domain.StyleRecommend;
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
public class StyleRecommendDto {
    private BigInteger id;
    private BigInteger recommendId;
    private String hairstyle;
    private String reason;
    private Integer price;
    private Date createDate;
    private Date updateDate;
    private int status;

    public StyleRecommendDto(StyleRecommend styleRecommend) {
        StyleRecommendDto.builder()
                .id(styleRecommend.getId())
                .recommendId(styleRecommend.getRecommend().getId())
                .hairstyle(styleRecommend.getHairstyle())
                .reason(styleRecommend.getReason())
                .price(styleRecommend.getPrice())
                .createDate(styleRecommend.getCreateDate())
                .updateDate(styleRecommend.getUpdateDate())
                .status(styleRecommend.getStatus())
                .build();
    }
}
