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
        this.id = styleRecommendImage.getId();
        this.styleRecommendId = styleRecommendImage.getStyleRecommend().getId();
        this.imageUrl = styleRecommendImage.getImageUrl();
        this.createDate = styleRecommendImage.getCreateDate();
        this.updateDate = styleRecommendImage.getUpdateDate();
        this.status = styleRecommendImage.getStatus();
    }
}
