package com.beforehairshop.demo.hairdesigner.dto;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerPrice;
import com.beforehairshop.demo.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigInteger;

@Getter
@AllArgsConstructor
public class HairDesignerPriceSaveRequestDto {
    private BigInteger hairDesignerId;
    private String hairCategory;
    private String hairStyleName;
    private Integer price;

    public HairDesignerPrice toEntity(Member hairDesigner) {
        return HairDesignerPrice.builder()
                .hairDesigner(hairDesigner)
                .hairCategory(hairCategory)
                .hairStyleName(hairStyleName)
                .price(price)
                .status(1)
                .build();
    }
}
