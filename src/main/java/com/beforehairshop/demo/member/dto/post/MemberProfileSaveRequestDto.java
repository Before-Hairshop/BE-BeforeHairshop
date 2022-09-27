package com.beforehairshop.demo.member.dto.post;

import com.beforehairshop.demo.constant.member.StatusKind;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfile;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
public class MemberProfileSaveRequestDto {

    private String name;
    private Integer hairCondition;
    private Integer hairTendency;
    private String desiredHairstyleDescription;

//    private String frontImageUrl;
//    private String sideImageUrl;
//    private String backImageUrl;

    private Integer payableAmount;
    private String zipCode;
    private String zipAddress;
    private Float latitude;
    private Float longitude;
    private String detailAddress;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date treatmentDate;
    private String phoneNumber;

    private List<DesiredHairstyleSaveRequestDto> desiredHairstyleList;

    public MemberProfile toEntity(Member member, String frontImageUrl, String sideImageUrl, String backImageUrl) {
        return MemberProfile.builder()
                .name(name)
                .member(member)
                .hairCondition(hairCondition)
                .hairTendency(hairTendency)
                .desiredHairstyleDescription(desiredHairstyleDescription)
                .frontImageUrl(frontImageUrl)
                .sideImageUrl(sideImageUrl)
                .backImageUrl(backImageUrl)
                .payableAmount(payableAmount)
                .zipCode(zipCode)
                .zipAddress(zipAddress)
                .latitude(latitude)
                .longitude(longitude)
                .detailAddress(detailAddress)
                .treatmentDate(treatmentDate)
                .phoneNumber(phoneNumber)
                .status(StatusKind.NORMAL.getId())
                .build();

    }

}
