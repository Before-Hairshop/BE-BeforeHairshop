package com.beforehairshop.demo.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MemberProfileImageResponseDto {
    private String frontPreSignedUrl;
    private String sidePreSignedUrl;
    private String backPreSignedUrl;
    private List<String> desiredStylePreSignedUrl;
}
