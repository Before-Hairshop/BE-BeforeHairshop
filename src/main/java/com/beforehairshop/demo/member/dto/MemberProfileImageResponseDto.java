package com.beforehairshop.demo.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberProfileImageResponseDto {
    private String frontPreSignedUrl;
    private String sidePreSignedUrl;
    private String backPreSignedUrl;
}
