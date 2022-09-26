package com.beforehairshop.demo.recommend.dto.patch;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StyleRecommendPatchRequestDto {
    private BigInteger styleRecommendId;
    private String hairstyle;
    private String reason;
    private Integer price;
}
