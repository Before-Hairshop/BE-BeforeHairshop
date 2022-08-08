package com.beforehairshop.demo.oauth.service;

import com.beforehairshop.demo.oauth.helper.constants.SocialLoginType;
import com.beforehairshop.demo.oauth.service.social.SocialOAuth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public String requestAccessToken(SocialLoginType socialLoginType, String code) {
        SocialOAuth socialOauth = this.findSocialOauthByType(socialLoginType);
        return socialOauth.requestAccessToken(code);
    }

    public String requestEmail(SocialLoginType socialLoginType, String accessToken) {
        SocialOAuth socialOauth = this.findSocialOauthByType(socialLoginType);
        return socialOauth.requestEmail(accessToken);
    }

    private SocialOAuth findSocialOauthByType(SocialLoginType socialLoginType) {
        return socialOAuthList.stream()
                .filter(x -> x.type() == socialLoginType)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 SocialLoginType 입니다."));
    }

//    public static JsonNode getGoogleUserInfo(String autorize_code) {
//
//        final String RequestUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
//
//        final HttpClient client = HttpClientBuilder.create().build();
//        final HttpGet get = new HttpGet(RequestUrl);
//
//        JsonNode returnNode = null;
//
//        // add header
//        get.addHeader("Authorization", "Bearer " + autorize_code);
//
//        try {
//            final HttpResponse response = client.execute(get);
//            final int responseCode = response.getStatusLine().getStatusCode();
//
//            ObjectMapper mapper = new ObjectMapper();
//            returnNode = mapper.readTree(response.getEntity().getContent());
//
//            System.out.println("\nSending 'GET' request to URL : " + RequestUrl);
//            System.out.println("Response Code : " + responseCode);
//
//
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (ClientProtocolException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            // clear resources
//        }
//
//
//
//        return returnNode;
//
//    }

}
