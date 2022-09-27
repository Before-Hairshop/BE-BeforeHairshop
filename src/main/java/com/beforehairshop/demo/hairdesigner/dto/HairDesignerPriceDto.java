package com.beforehairshop.demo.hairdesigner.dto;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerPrice;
import com.beforehairshop.demo.member.domain.Member;
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
public class HairDesignerPriceDto {
    private BigInteger id;
    private BigInteger hairDesignerId;
    private String hairCategory;
    private String hairStyleName;
    private Integer price;
    private Date createDate;
    private Date updateDate;
    private int status;

    public HairDesignerPriceDto(HairDesignerPrice hairDesignerPrice) {
        HairDesignerPriceDto.builder()
                .id(hairDesignerPrice.getId())
                .hairDesignerId(hairDesignerPrice.getHairDesigner().getId())
                .hairCategory(hairDesignerPrice.getHairCategory())
                .hairStyleName(hairDesignerPrice.getHairStyleName())
                .price(hairDesignerPrice.getPrice())
                .createDate(hairDesignerPrice.getCreateDate())
                .updateDate(hairDesignerPrice.getUpdateDate())
                .status(hairDesignerPrice.getStatus())
                .build();
    }
}
