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
    private String desiredHairstyle;
    private String desiredHairstyleDescription;
    private String frontImageUrl;
    private String sideImageUrl;
    private String backImageUrl;
    private Integer payableAmount;
    private String zipCode;
    private String zipAddress;
    private Float latitude;
    private Float longitude;
    private String phoneNumber;
    private Date treatmentDate;
    private Integer matchingActivationFlag;
    private Date createDate;
    private Date updateDate;
    private int status;

    public MemberProfileDto(MemberProfile memberProfile) {
        this.id = memberProfile.getId();
        this.memberId = memberProfile.getMember().getId();
        this.name = memberProfile.getName();
        this.hairCondition = memberProfile.getHairCondition();
        this.hairTendency = memberProfile.getHairTendency();
        this.desiredHairstyle = memberProfile.getDesiredHairstyle();
        this.desiredHairstyleDescription = memberProfile.getDesiredHairstyleDescription();
        this.frontImageUrl = memberProfile.getFrontImageUrl();
        this.sideImageUrl = memberProfile.getSideImageUrl();
        this.backImageUrl = memberProfile.getBackImageUrl();
        this.payableAmount = memberProfile.getPayableAmount();
        this.zipCode = memberProfile.getZipCode();
        this.zipAddress = memberProfile.getZipAddress();
        this.latitude = memberProfile.getLatitude();
        this.longitude = memberProfile.getLongitude();
        this.phoneNumber = memberProfile.getPhoneNumber();
        this.treatmentDate = memberProfile.getTreatmentDate();
        this.matchingActivationFlag = memberProfile.getMatchingActivationFlag();
        this.createDate = memberProfile.getCreateDate();
        this.updateDate = memberProfile.getUpdateDate();
        this.status = memberProfile.getStatus();

    }

//    public static MemberProfileDto toDto(MemberProfile memberProfile) {
//        return MemberProfileDto.builder()
//                .id(memberProfile.getId())
//                .memberId(memberProfile.getMember().getId())
//                .name(memberProfile.getName())
//                .hairCondition(memberProfile.getHairCondition())
//                .hairTendency(memberProfile.getHairTendency())
//                .desiredHairstyleDescription(memberProfile.getDesiredHairstyleDescription())
//                .frontImageUrl(memberProfile.getFrontImageUrl())
//                .sideImageUrl(memberProfile.getSideImageUrl())
//                .backImageUrl(memberProfile.getBackImageUrl())
//                .payableAmount(memberProfile.getPayableAmount())
//                .zipCode(memberProfile.getZipCode())
//                .zipAddress(memberProfile.getZipAddress())
//                .latitude(memberProfile.getLatitude())
//                .longitude(memberProfile.getLongitude())
//                .detailAddress(memberProfile.getDetailAddress())
//                .phoneNumber(memberProfile.getPhoneNumber())
//                .treatmentDate(memberProfile.getTreatmentDate())
//                .matchingActivationFlag(memberProfile.getMatchingActivationFlag())
//                .createDate(memberProfile.getCreateDate())
//                .updateDate(memberProfile.getUpdateDate())
//                .status(memberProfile.getStatus())
//                .build();
//    }
}
