package com.beforehairshop.demo.member.dto;

import com.beforehairshop.demo.member.domain.MemberProfile;
import com.beforehairshop.demo.member.domain.MemberProfileDesiredHairstyle;
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
public class MemberProfileDesiredHairstyleDto {
    private BigInteger id;
    private BigInteger memberProfileId;
    private String desiredHairstyle;
    private Date createDate;
    private Date updateDate;
    private int status;

    public MemberProfileDesiredHairstyleDto(MemberProfileDesiredHairstyle memberProfileDesiredHairstyle) {
        MemberProfileDesiredHairstyleDto.builder()
                .id(memberProfileDesiredHairstyle.getId())
                .memberProfileId(memberProfileDesiredHairstyle.getMemberProfile().getId())
                .desiredHairstyle(memberProfileDesiredHairstyle.getDesiredHairstyle())
                .createDate(memberProfileDesiredHairstyle.getCreateDate())
                .updateDate(memberProfileDesiredHairstyle.getUpdateDate())
                .status(memberProfileDesiredHairstyle.getStatus())
                .build();
    }
}
