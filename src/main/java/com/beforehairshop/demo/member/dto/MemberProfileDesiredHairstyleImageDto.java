package com.beforehairshop.demo.member.dto;

import com.beforehairshop.demo.member.domain.MemberProfile;
import com.beforehairshop.demo.member.domain.MemberProfileDesiredHairstyleImage;
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
public class MemberProfileDesiredHairstyleImageDto {
    private BigInteger id;
    private BigInteger memberProfileId;
    private String imageUrl;
    private Date createDate;
    private Date updateDate;
    private int status;

    public MemberProfileDesiredHairstyleImageDto(MemberProfileDesiredHairstyleImage memberProfileDesiredHairstyleImage) {
        this.id = memberProfileDesiredHairstyleImage.getId();
        this.memberProfileId = memberProfileDesiredHairstyleImage.getMemberProfile().getId();
        this.imageUrl = memberProfileDesiredHairstyleImage.getImageUrl();
        this.createDate = memberProfileDesiredHairstyleImage.getCreateDate();
        this.updateDate = memberProfileDesiredHairstyleImage.getUpdateDate();
        this.status = memberProfileDesiredHairstyleImage.getStatus();
    }
}
