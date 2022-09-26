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
        MemberProfileDesiredHairstyleImageDto.builder()
                .id(memberProfileDesiredHairstyleImage.getId())
                .memberProfileId(memberProfileDesiredHairstyleImage.getMemberProfile().getId())
                .imageUrl(memberProfileDesiredHairstyleImage.getImageUrl())
                .createDate(memberProfileDesiredHairstyleImage.getCreateDate())
                .updateDate(memberProfileDesiredHairstyleImage.getUpdateDate())
                .status(memberProfileDesiredHairstyleImage.getStatus())
                .build();
    }
}
