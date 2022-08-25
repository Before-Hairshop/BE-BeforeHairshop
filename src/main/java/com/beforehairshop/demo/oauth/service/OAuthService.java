package com.beforehairshop.demo.oauth.service;

import com.beforehairshop.demo.oauth.dto.OAuthDto;
import com.beforehairshop.demo.oauth.helper.constants.SocialLoginType;
import com.beforehairshop.demo.oauth.helper.parser.JsonParser;
import com.beforehairshop.demo.oauth.service.social.SocialOAuth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OAuthService {
    private final List<SocialOAuth> socialOAuthList;
    private final HttpServletResponse response;

    public void request(SocialLoginType socialLoginType) {
        SocialOAuth socialOauth = this.findSocialOauthByType(socialLoginType);
        String redirectURL = socialOauth.getOauthRedirectURL();
        try {
            response.sendRedirect(redirectURL);
            log.info("success of sending Redirect url");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public OAuthDto requestAccessToken(SocialLoginType socialLoginType, String code) {
        SocialOAuth socialOauth = this.findSocialOauthByType(socialLoginType);
        return JsonParser.parseTokenInfo(socialOauth.requestAccessToken(code));
    }

    public String requestEmail(SocialLoginType socialLoginType, String accessToken) {
        SocialOAuth socialOauth = this.findSocialOauthByType(socialLoginType);
        if (socialLoginType.compareTo(SocialLoginType.KAKAO) == 0) {
            JsonParser.parseKakaoEmailInfo(socialOauth.requestEmail(accessToken));
        }
        return JsonParser.parseGoogleEmailInfo(socialOauth.requestEmail(accessToken));
    }

    private SocialOAuth findSocialOauthByType(SocialLoginType socialLoginType) {
        return socialOAuthList.stream()
                .filter(x -> x.type() == socialLoginType)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 SocialLoginType 입니다."));
    }

}
