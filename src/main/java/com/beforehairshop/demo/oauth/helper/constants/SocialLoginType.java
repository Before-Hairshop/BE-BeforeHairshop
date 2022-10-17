package com.beforehairshop.demo.oauth.helper.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SocialLoginType {
    APPLE("apple", "소셜 로그인 - 애플"),
    KAKAO("kakao", "소셜 로그인 - 카카오");

    private String provider;
    private String description;

}
