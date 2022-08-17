package com.beforehairshop.demo.oauth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OAuthDto {
    private Long id;
    private String email;
    private String accessToken;
    private String refreshToken;
    private Long expireTime;

}
