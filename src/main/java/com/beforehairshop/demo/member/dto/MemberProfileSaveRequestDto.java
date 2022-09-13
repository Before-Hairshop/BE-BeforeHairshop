package com.beforehairshop.demo.member.dto;

import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
public class MemberProfileSaveRequestDto {

    private Integer hairCondition;
    private Integer hairTendency;
    private String desiredHairstyle;
    private String desiredHairstyleDescription;

    private MultipartFile frontImage;
    private MultipartFile sideImage;
    private MultipartFile backImage;
//
//    private String frontImageUrl;
//    private String sideImageUrl;
//    private String backImageUrl;

    private Integer payableAmount;
    private Integer zipCode;
    private String zipAddress;
    private Float latitude;
    private Float longitude;
    private String detailAddress;

    public MemberProfile toEntity(Member member, String frontImageUrl, String sideImageUrl, String backImageUrl) {
        return MemberProfile.builder()
                .member(member)
                .hairCondition(hairCondition)
                .hairTendency(hairTendency)
                .desiredHairstyle(desiredHairstyle)
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
                .status(1)
                .build();

    }
}