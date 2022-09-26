package com.beforehairshop.demo.member.dto;

import com.beforehairshop.demo.member.domain.MemberProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.persistence.Column;
import java.math.BigInteger;
import java.util.Date;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileDto {
    private BigInteger id;
    private BigInteger memberId;
    private String name;
    private Integer hairCondition;
    private Integer hairTendency;
    private String desiredHairstyleDescription;
    private String frontImageUrl;
    private String sideImageUrl;
    private String backImageUrl;
    private Integer payableAmount;
    private String zipCode;
    private String zipAddress;
    private Float latitude;
    private Float longitude;
    private String detailAddress;
    private String phoneNumber;
    private Date treatmentDate;
    private Date createDate;
    private Date updateDate;
    private int status;

    public MemberProfileDto(MemberProfile memberProfile) {
        MemberProfileDto.builder()
                .id(memberProfile.getId())
                .memberId(memberProfile.getMember().getId())
                .name(memberProfile.getName())
                .hairCondition(memberProfile.getHairCondition())
                .hairTendency(memberProfile.getHairTendency())
                .desiredHairstyleDescription(memberProfile.getDesiredHairstyleDescription())
                .frontImageUrl(memberProfile.getFrontImageUrl())
                .sideImageUrl(memberProfile.getSideImageUrl())
                .backImageUrl(memberProfile.getBackImageUrl())
                .payableAmount(memberProfile.getPayableAmount())
                .zipCode(memberProfile.getZipCode())
                .zipAddress(memberProfile.getZipAddress())
                .latitude(memberProfile.getLatitude())
                .longitude(memberProfile.getLongitude())
                .detailAddress(memberProfile.getDetailAddress())
                .phoneNumber(memberProfile.getPhoneNumber())
                .treatmentDate(memberProfile.getTreatmentDate())
                .createDate(memberProfile.getCreateDate())
                .updateDate(memberProfile.getUpdateDate())
                .status(memberProfile.getStatus())
                .build();
    }
}
