package com.beforehairshop.demo.oauth.controller;

import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.member.service.MemberService;
import com.beforehairshop.demo.oauth.dto.post.AppleUserSaveRequestDto;
import com.beforehairshop.demo.oauth.dto.post.KakaoUserSaveRequestDto;
import com.beforehairshop.demo.oauth.service.OAuthService;
import com.beforehairshop.demo.response.ResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.management.relation.RelationSupport;
import javax.servlet.http.HttpServletRequest;

@RestController
@Tag(name = "모든 유저 관련 Controller")
@AllArgsConstructor
@RequestMapping("/api/v1/oauth")
public class OAuthController {

    private final OAuthService oAuthService;
    private final MemberService memberService;

    @PostMapping("kakao")
    @Operation(summary = "카카오 로그인 API")
    public ResponseEntity<ResultDto> signInKakao(@RequestBody KakaoUserSaveRequestDto saveRequestDto) {
        return oAuthService.signInKakao(saveRequestDto);
    }

    @PostMapping("apple")
    @Operation(summary = "애플 로그인 API")
    public ResponseEntity<ResultDto> signInApple(@RequestBody AppleUserSaveRequestDto saveRequestDto) {
        return oAuthService.signInApple(saveRequestDto);
    }

    @PatchMapping("logout")
    @Operation(summary = "로그아웃 (세션 삭제)")
    public ResponseEntity<ResultDto> logout(@AuthenticationPrincipal PrincipalDetails principalDetails, HttpServletRequest request) {
        return oAuthService.logout(principalDetails.getMember(), request);
    }



//    /**
//     * 사용자로부터 SNS 로그인 요청을 Social Login Type 을 받아 처리
//     * @param socialLoginType (GOOGLE, KAKAO)
//     */
//    @GetMapping(value = "/{socialLoginType}")
//    public void socialLoginType(
//            @PathVariable(name = "socialLoginType") SocialLoginType socialLoginType) {
//        log.info(">> 사용자로부터 SNS 로그인 요청을 받음 :: {} Social Login", socialLoginType);
//        oAuthService.request(socialLoginType);
//    }
//
//    /**
//     * Social Login API Server 요청에 의한 callback 을 처리
//     * @param socialLoginType (GOOGLE, KAKAO)
//     * @param code API Server 로부터 넘어오는 code
//     * @return SNS Login 요청 결과로 받은 Json 형태의 String 문자열 (access_token, refresh_token 등)
//     */
//    @GetMapping(value = "/{socialLoginType}/callback")
//    public ResponseEntity<ResultDto> callback(
//            @PathVariable(name = "socialLoginType") SocialLoginType socialLoginType,
//            @RequestParam(name = "code") String code) {
//        log.info(">> 로그인 유형 :: {}", socialLoginType);
//        log.info(">> 소셜 로그인 API 서버로부터 받은 code :: {}", code);
//        //String tokenInfoFromGoogle = oAuthService.requestAccessToken(socialLoginType, code);
//
//        //해당 메서드에서 파싱까지 해주고 oauthDto return 해준다.
//        OAuthDto oAuthDto = oAuthService.requestAccessToken(socialLoginType, code);
//
//        // 해당 메서드 아래에서 request 이후에 파싱까지 해주도록 한다.
//        oAuthDto.setEmail(oAuthService.requestEmail(socialLoginType, oAuthDto.getAccessToken()));
//
//
//        log.info(oAuthDto.toString());
//        // member save
//        if (oAuthDto.getEmail() != null) {
//            BigInteger id = memberService.save(new MemberSaveRequestDto(oAuthDto.getEmail(), socialLoginType.toString(), 1));
//            oAuthDto.setId(id);
//            return makeResult(HttpStatus.OK, oAuthDto);
//        }
//
//        log.error("Fail to save member. Because email info is null");
//        return makeResult(HttpStatus.INTERNAL_SERVER_ERROR, null);
//    }

}
