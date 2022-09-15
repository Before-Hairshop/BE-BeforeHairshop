package com.beforehairshop.demo.hairdesigner.dto.patch;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerPrice;
import com.beforehairshop.demo.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HairDesignerPricePatchRequestDto {
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
