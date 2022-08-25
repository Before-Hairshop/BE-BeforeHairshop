package com.beforehairshop.demo.oauth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
public class OAuthDto {
    private BigInteger id;
    private String email;
    private String accessToken;
    private String refreshToken;
    private Long expireTime;

}
