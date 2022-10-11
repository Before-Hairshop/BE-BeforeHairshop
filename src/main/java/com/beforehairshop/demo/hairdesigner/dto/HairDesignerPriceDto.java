package com.beforehairshop.demo.hairdesigner.dto;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerPrice;
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
public class HairDesignerPriceDto {
    private BigInteger id;
    private BigInteger hairDesignerProfileId;
    private String hairCategory;
    private String hairStyleName;
    private Integer price;
    private Date createDate;
    private Date updateDate;
    private int status;

    public HairDesignerPriceDto(HairDesignerPrice hairDesignerPrice) {
        this.id = hairDesignerPrice.getId();
        this.hairDesignerProfileId = hairDesignerPrice.getHairDesignerProfile().getId();
        this.hairCategory = hairDesignerPrice.getHairCategory();
        this.hairStyleName = hairDesignerPrice.getHairStyleName();
        this.price = hairDesignerPrice.getPrice();
        this.createDate = hairDesignerPrice.getCreateDate();
        this.updateDate = hairDesignerPrice.getUpdateDate();
        this.status = hairDesignerPrice.getStatus();
    }
}
