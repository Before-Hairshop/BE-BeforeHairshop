package com.beforehairshop.demo.hairdesigner.dto;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.member.domain.Member;
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
public class HairDesignerProfileDto {
    private BigInteger id;
    private BigInteger hairDesignerId;
    private String imageUrl;
    private String name;
    private String description;
    private String hairShopName;
    private String zipCode;  // 우편번호
    private String zipAddress;  // 우편번호에 해당하는 주소
    private Float latitude;  // 위도
    private Float longitude;  // 경도
    private String detailAddress;  // 상세주소
    private String phoneNumber;
    private Date createDate;
    private Date updateDate;
    private int status;

    public HairDesignerProfileDto(HairDesignerProfile hairDesignerProfile) {
        HairDesignerProfileDto.builder()
                .id(hairDesignerProfile.getId())
                .hairDesignerId(hairDesignerProfile.getHairDesigner().getId())
                .imageUrl(hairDesignerProfile.getImageUrl())
                .name(hairDesignerProfile.getName())
                .description(hairDesignerProfile.getDescription())
                .hairShopName(hairDesignerProfile.getHairShopName())
                .zipCode(hairDesignerProfile.getZipCode())
                .zipAddress(hairDesignerProfile.getZipAddress())
                .latitude(hairDesignerProfile.getLatitude())
                .longitude(hairDesignerProfile.getLongitude())
                .detailAddress(hairDesignerProfile.getDetailAddress())
                .phoneNumber(hairDesignerProfile.getPhoneNumber())
                .createDate(hairDesignerProfile.getCreateDate())
                .updateDate(hairDesignerProfile.getUpdateDate())
                .status(hairDesignerProfile.getStatus())
                .build();
    }
}
