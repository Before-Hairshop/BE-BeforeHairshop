package com.beforehairshop.demo.oauth.controller;

import com.beforehairshop.demo.member.dto.MemberSaveRequestDto;
import com.beforehairshop.demo.member.service.MemberService;
import com.beforehairshop.demo.oauth.dto.OAuthDto;
import com.beforehairshop.demo.oauth.helper.constants.SocialLoginType;
import com.beforehairshop.demo.oauth.service.OAuthService;
import com.beforehairshop.demo.response.ResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.beforehairshop.demo.response.ResultDto.makeResult;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping(value = "/auth")
@Slf4j
public class OAuthController {

    private final OAuthService oAuthService;
    private final MemberService memberService;

    /**
     * 사용자로부터 SNS 로그인 요청을 Social Login Type 을 받아 처리
     * @param socialLoginType (GOOGLE, KAKAO)
     */
    @GetMapping(value = "/{socialLoginType}")
    public void socialLoginType(
            @PathVariable(name = "socialLoginType") SocialLoginType socialLoginType) {
        log.info(">> 사용자로부터 SNS 로그인 요청을 받음 :: {} Social Login", socialLoginType);
        oAuthService.request(socialLoginType);
    }

    /**
     * Social Login API Server 요청에 의한 callback 을 처리
     * @param socialLoginType (GOOGLE, KAKAO)
     * @param code API Server 로부터 넘어오는 code
     * @return SNS Login 요청 결과로 받은 Json 형태의 String 문자열 (access_token, refresh_token 등)
     */
    @GetMapping(value = "/{socialLoginType}/callback")
    public ResponseEntity<ResultDto> callback(
            @PathVariable(name = "socialLoginType") SocialLoginType socialLoginType,
            @RequestParam(name = "code") String code) {
        log.info(">> 소셜 로그인 API 서버로부터 받은 code :: {}", code);
        //String tokenInfoFromGoogle = oAuthService.requestAccessToken(socialLoginType, code);

        //해당 메서드에서 파싱까지 해주고 oauthDto return 해준다.
        OAuthDto oAuthDto = oAuthService.requestAccessToken(socialLoginType, code);

        // 해당 메서드 아래에서 request 이후에 파싱까지 해주도록 한다.
        oAuthDto.setEmail(oAuthService.requestEmail(socialLoginType, oAuthDto.getAccessToken()));


        // member save
        if (oAuthDto.getEmail() != null) {
            Long id = memberService.save(new MemberSaveRequestDto(oAuthDto.getEmail(), socialLoginType.toString(), 1));
            oAuthDto.setId(id);
            return makeResult(HttpStatus.OK, oAuthDto);
        }

        log.error("Fail to save member. Because email info is null");
        return makeResult(HttpStatus.INTERNAL_SERVER_ERROR, null);

    }

}