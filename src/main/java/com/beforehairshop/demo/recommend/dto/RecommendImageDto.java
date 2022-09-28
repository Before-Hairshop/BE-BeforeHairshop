package com.beforehairshop.demo.recommend.dto;

import com.beforehairshop.demo.recommend.domain.RecommendImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.Date;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendImageDto {
    private BigInteger id;
    private BigInteger recommendId;
    private String imageUrl;
    private Date createDate;
    private Date updateDate;
    private int status;


    public RecommendImageDto(RecommendImage recommendImage) {
        this.id = recommendImage.getId();
        this.recommendId = recommendImage.getRecommend().getId();
        this.imageUrl = recommendImage.getImageUrl();
        this.createDate = recommendImage.getCreateDate();
        this.updateDate = recommendImage.getUpdateDate();
        this.status = recommendImage.getStatus();
    }
}
