package com.beforehairshop.demo.hairdesigner.dto;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerHashtag;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.math.BigInteger;
import java.util.List;

@Getter
@AllArgsConstructor
public class HairDesignerProfileSaveRequestDto {
    private MultipartFile image;
    private String name;
    private String description;
    private String hairShopName;
    private Integer zipCode;  // 우편번호
    private String zipAddress;  // 우편번호에 해당하는 주소
    private Float latitude;  // 위도
    private Float longitude;  // 경도
    private String detailAddress;  // 상세주소
    private String phoneNumber;

    private List<HairDesignerHashtagSaveRequestDto> hashtagList;
    private List<HairDesignerWorkingDaySaveRequestDto> workingDayList;
    private List<HairDesignerPriceSaveRequestDto> priceList;

    public HairDesignerProfile toEntity(Member hairDesigner, String imageUrl) {
        return HairDesignerProfile.builder()
                .member(hairDesigner)
                .name(name)
                .imageUrl(imageUrl)
                .description(description)
                .hairShopName(hairShopName)
                .zipCode(zipCode)
                .zipAddress(zipAddress)
                .latitude(latitude)
                .longitude(longitude)
                .detailAddress(detailAddress)
                .phoneNumber(phoneNumber)
                .status(1)
                .build();
    }
}
