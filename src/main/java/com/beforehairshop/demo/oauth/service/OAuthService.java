package com.beforehairshop.demo.oauth.service;

import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.dto.MemberDto;
import com.beforehairshop.demo.member.repository.MemberRepository;
import com.beforehairshop.demo.oauth.dto.OAuthDto;
import com.beforehairshop.demo.oauth.dto.post.AppleUserSaveRequestDto;
import com.beforehairshop.demo.oauth.dto.post.KakaoUserSaveRequestDto;
import com.beforehairshop.demo.oauth.helper.constants.SocialLoginType;
import com.beforehairshop.demo.oauth.helper.parser.JsonParser;
import com.beforehairshop.demo.oauth.service.social.SocialOAuth;
import com.beforehairshop.demo.response.ResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.beforehairshop.demo.response.ResultDto.makeResult;

@Service
@Slf4j
@RequiredArgsConstructor
public class OAuthService {

    private final MemberRepository memberRepository;

    @Transactional
    public ResponseEntity<ResultDto> signInKakao(KakaoUserSaveRequestDto saveRequestDto) {
        Member member = memberRepository.findByUsername(
                createUsernameInKakao(saveRequestDto.getProviderId())
        );

        if (member != null) {
            List<GrantedAuthority> preUpdatedAuthorities = new ArrayList<>();
            preUpdatedAuthorities.add(new SimpleGrantedAuthority(member.getRole()));

            Authentication preAuthentication = new UsernamePasswordAuthenticationToken(
                    new PrincipalDetails(member)
                    , null
                    , preUpdatedAuthorities
            );

            SecurityContextHolder.getContext().setAuthentication(preAuthentication);
            return makeResult(HttpStatus.FOUND, new MemberDto(member));
        }

        Member newMember = saveRequestDto.toEntity(createUsernameInApple(saveRequestDto.getProviderId()));
        newMember = memberRepository.save(newMember);

        List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
        updatedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new PrincipalDetails(newMember)
                , null
                , updatedAuthorities
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return makeResult(HttpStatus.OK, new MemberDto(newMember));

    }

    private String createUsernameInKakao(String providerId) {
        return SocialLoginType.KAKAO.getProvider() + "_" + providerId;
    }

    private String createUsernameInApple(String providerId) {
        return SocialLoginType.APPLE.getProvider() + "_" + providerId;
    }

    @Transactional
    public ResponseEntity<ResultDto> signInApple(AppleUserSaveRequestDto saveRequestDto) {
        Member member = memberRepository.findByUsername(
                createUsernameInApple(saveRequestDto.getProviderId())
        );

        if (member != null) {
            List<GrantedAuthority> preUpdatedAuthorities = new ArrayList<>();
            preUpdatedAuthorities.add(new SimpleGrantedAuthority(member.getRole()));

            Authentication preAuthentication = new UsernamePasswordAuthenticationToken(
                    new PrincipalDetails(member)
                    , null
                    , preUpdatedAuthorities
            );

            SecurityContextHolder.getContext().setAuthentication(preAuthentication);
            return makeResult(HttpStatus.FOUND, new MemberDto(member));
        }

        Member newMember = saveRequestDto.toEntity(createUsernameInApple(saveRequestDto.getProviderId()));
        newMember = memberRepository.save(newMember);

        List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
        updatedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new PrincipalDetails(newMember)
                , null
                , updatedAuthorities
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return makeResult(HttpStatus.OK, new MemberDto(newMember));
    }

    @Transactional
    public ResponseEntity<ResultDto> logout(Member member, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        session.invalidate();
        // 시큐리티 인증정보 없애기
        SecurityContextHolder.getContext().setAuthentication(null);

        return makeResult(HttpStatus.OK, "세션 삭제 완료");
    }

//    private final List<SocialOAuth> socialOAuthList;
//    private final HttpServletResponse response;
//
//    public void request(SocialLoginType socialLoginType) {
//        SocialOAuth socialOauth = this.findSocialOauthByType(socialLoginType);
//        String redirectURL = socialOauth.getOauthRedirectURL();
//        try {
//            response.sendRedirect(redirectURL);
//            log.info("success of sending Redirect url");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public OAuthDto requestAccessToken(SocialLoginType socialLoginType, String code) {
//        SocialOAuth socialOauth = this.findSocialOauthByType(socialLoginType);
//        return JsonParser.parseTokenInfo(socialOauth.requestAccessToken(code));
//    }
//
//    public String requestEmail(SocialLoginType socialLoginType, String accessToken) {
//        SocialOAuth socialOauth = this.findSocialOauthByType(socialLoginType);
////        System.out.println(socialLoginType.compareTo(SocialLoginType.KAKAO));
//        if (socialLoginType.compareTo(SocialLoginType.KAKAO) == 0) {
//            return JsonParser.parseKakaoEmailInfo(socialOauth.requestEmail(accessToken));
//        }
//        return JsonParser.parseGoogleEmailInfo(socialOauth.requestEmail(accessToken));
//    }
//
//    private SocialOAuth findSocialOauthByType(SocialLoginType socialLoginType) {
//        return socialOAuthList.stream()
//                .filter(x -> x.type() == socialLoginType)
//                .findFirst()
//                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 SocialLoginType 입니다."));
//    }

}
