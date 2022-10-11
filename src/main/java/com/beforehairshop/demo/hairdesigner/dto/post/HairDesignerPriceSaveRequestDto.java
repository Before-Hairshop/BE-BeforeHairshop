package com.beforehairshop.demo.hairdesigner.dto.post;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerPrice;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HairDesignerPriceSaveRequestDto {
    private String hairCategory;
    private String hairStyleName;
    private Integer price;

    public HairDesignerPrice toEntity(HairDesignerProfile hairDesignerProfile) {
        return HairDesignerPrice.builder()
                .hairDesignerProfile(hairDesignerProfile)
                .hairCategory(hairCategory)
                .hairStyleName(hairStyleName)
                .price(price)
                .status(1)
                .build();
    }
}
