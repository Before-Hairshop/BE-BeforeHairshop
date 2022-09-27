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
        this.id = hairDesignerPrice.getId();
        this.hairDesignerId = hairDesignerPrice.getHairDesigner().getId();
        this.hairCategory = hairDesignerPrice.getHairCategory();
        this.hairStyleName = hairDesignerPrice.getHairStyleName();
        this.price = hairDesignerPrice.getPrice();
        this.createDate = hairDesignerPrice.getCreateDate();
        this.updateDate = hairDesignerPrice.getUpdateDate();
        this.status = hairDesignerPrice.getStatus();
    }
}
