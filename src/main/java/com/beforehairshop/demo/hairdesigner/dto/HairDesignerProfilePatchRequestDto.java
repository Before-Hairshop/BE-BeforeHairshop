package com.beforehairshop.demo.hairdesigner.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
public class HairDesignerProfilePatchRequestDto {
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
}
