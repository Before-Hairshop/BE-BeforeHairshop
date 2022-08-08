package com.beforehairshop.demo.oauth.controller;

import com.beforehairshop.demo.member.service.MemberService;
import com.beforehairshop.demo.oauth.helper.constants.SocialLoginType;
import com.beforehairshop.demo.oauth.service.OAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static com.beforehairshop.demo.secret.social.SecretGoogle.getGoogleSnsTokenBaseUrl;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping(value = "/auth")
@Slf4j
public class OAuthController {

    private final OAuthService oauthService;
    private final MemberService memberService;

    /**
     * 사용자로부터 SNS 로그인 요청을 Social Login Type 을 받아 처리
     * @param socialLoginType (GOOGLE, KAKAO)
     */
    @GetMapping(value = "/{socialLoginType}")
    public void socialLoginType(
            @PathVariable(name = "socialLoginType") SocialLoginType socialLoginType) {
        log.info(">> 사용자로부터 SNS 로그인 요청을 받음 :: {} Social Login", socialLoginType);
        oauthService.request(socialLoginType);
    }

    /**
     * Social Login API Server 요청에 의한 callback 을 처리
     * @param socialLoginType (GOOGLE, KAKAO)
     * @param code API Server 로부터 넘어오는 code
     * @return SNS Login 요청 결과로 받은 Json 형태의 String 문자열 (access_token, refresh_token 등)
     */
    @GetMapping(value = "/{socialLoginType}/callback")
    public String callback(
            @PathVariable(name = "socialLoginType") SocialLoginType socialLoginType,
            @RequestParam(name = "code") String code) {
        log.info(">> 소셜 로그인 API 서버로부터 받은 code :: {}", code);
        String tokenInfoFromGoogle = oauthService.requestAccessToken(socialLoginType, code);
        //log.info("access_token : " + returnValueFromGoogle);

        JSONParser jsonParser = new JSONParser();
        String accessToken = null;

        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(tokenInfoFromGoogle);
            accessToken = (String) jsonObject.get("access_token");
            String refreshToken = (String) jsonObject.get("refresh_token");
            Long expireTime = (Long) jsonObject.get("expires_in");
            String scope = (String) jsonObject.get("scope");
            log.info("access token : " + accessToken);
            log.info("refresh token : " + refreshToken);
            log.info("expires_in : " + expireTime);
            log.info("scope : " + scope);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        // 해당 메서드에 아래 로직 포함시킨다.
        String emailFromGoogle = null;
        String email = null;
        try {
            emailFromGoogle = oauthService.requestEmail(SocialLoginType.GOOGLE, accessToken);
        } catch (Exception e) {
            log.error("Fail to get email from Google API Server");
        }

        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(emailFromGoogle);
            email = (String) jsonObject.get("email");
            log.info("email : " + email);

        }
        catch (ParseException e) {
            log.error("Fail to json parsing");
        }

        System.out.println(responseEntity);
        return responseEntity;

//        if (responseEntity.getStatusCode() == HttpStatus.OK) {
//            return responseEntity.getBody();
//        } else {
//            return "fail";
//        }

        //return returnValueFromGoogle;
    }

}
