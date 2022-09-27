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
        HairDesignerHashtagDto.builder()
                .id(hairDesignerHashtag.getId())
                .hairDesignerId(hairDesignerHashtag.getHairDesigner().getId())
                .tag(hairDesignerHashtag.getTag())
                .createDate(hairDesignerHashtag.getCreateDate())
                .updateDate(hairDesignerHashtag.getUpdateDate())
                .status(hairDesignerHashtag.getStatus())
                .build();
    }
}
