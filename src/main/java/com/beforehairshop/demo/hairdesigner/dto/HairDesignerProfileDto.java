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
        this.id = hairDesignerProfile.getId();
        this.hairDesignerId = hairDesignerProfile.getHairDesigner().getId();
        this.imageUrl = hairDesignerProfile.getImageUrl();
        this.name = hairDesignerProfile.getName();
        this.description = hairDesignerProfile.getDescription();
        this.hairShopName = hairDesignerProfile.getHairShopName();
        this.zipCode = hairDesignerProfile.getZipCode();
        this.zipAddress = hairDesignerProfile.getZipAddress();
        this.latitude = hairDesignerProfile.getLatitude();
        this.longitude = hairDesignerProfile.getLongitude();
        this.detailAddress = hairDesignerProfile.getDetailAddress();
        this.phoneNumber = hairDesignerProfile.getPhoneNumber();
        this.createDate = hairDesignerProfile.getCreateDate();
        this.updateDate = hairDesignerProfile.getUpdateDate();
        this.status = hairDesignerProfile.getStatus();
    }
}
