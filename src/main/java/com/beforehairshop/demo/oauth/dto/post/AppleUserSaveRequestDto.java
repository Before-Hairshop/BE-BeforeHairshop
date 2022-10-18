package com.beforehairshop.demo.oauth.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AppleUserSaveRequestDto {
    private String providerId;
    private String email;
    private String accessToken;
}
