package com.beforehairshop.demo.oauth.service.social;

import org.springframework.stereotype.Component;

@Component
public class KakaoOAuth implements SocialOAuth {
    @Override
    public String getOauthRedirectURL() {
        return "";
    }

    @Override
    public String requestAccessToken(String code) {
        return null;
    }
}
