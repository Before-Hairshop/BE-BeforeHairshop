package com.beforehairshop.demo.recommend.dto;

import com.beforehairshop.demo.recommend.domain.RecommendRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendRequestDto {
    private BigInteger id;
    private BigInteger toRecommendRequestProfileId;
    private BigInteger fromRecommendRequestProfileId;
    private Date createDate;
    private Date updateDate;
    private Integer status;

    public RecommendRequestDto(RecommendRequest recommendRequest) {
        this.id = recommendRequest.getId();
        this.toRecommendRequestProfileId = recommendRequest.getToRecommendRequestProfile().getId();
        this.fromRecommendRequestProfileId = recommendRequest.getFromRecommendRequestProfile().getId();
        this.createDate = recommendRequest.getCreateDate();
        this.updateDate = recommendRequest.getUpdateDate();
        this.status = recommendRequest.getStatus();
    }
}
