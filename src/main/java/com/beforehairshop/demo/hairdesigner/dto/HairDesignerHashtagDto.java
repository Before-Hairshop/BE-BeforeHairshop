package com.beforehairshop.demo.hairdesigner.dto;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerHashtag;
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
public class HairDesignerHashtagDto {
    private BigInteger id;
    private BigInteger hairDesignerId;
    private String tag;
    private Date createDate;
    private Date updateDate;
    private int status;

    public HairDesignerHashtagDto(HairDesignerHashtag hairDesignerHashtag) {
        this.id = hairDesignerHashtag.getId();
        this.hairDesignerId = hairDesignerHashtag.getHairDesigner().getId();
        this.tag = hairDesignerHashtag.getTag();
        this.createDate = hairDesignerHashtag.getCreateDate();
        this.updateDate = hairDesignerHashtag.getUpdateDate();
        this.status = hairDesignerHashtag.getStatus();
    }
}
