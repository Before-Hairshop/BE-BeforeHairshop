package com.beforehairshop.demo.oauth;

import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.repository.MemberRepository;
import com.beforehairshop.demo.oauth.provider.GoogleUserInfo;
import com.beforehairshop.demo.oauth.provider.KakaoUserInfo;
import com.beforehairshop.demo.oauth.provider.NaverUserInfo;
import com.beforehairshop.demo.oauth.provider.OAuth2UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private MemberRepository memberRepository;


    /**
     * 구글로부터 받은 userRequest 데이터에 대해 후처리해주는 함수
     * @param userRequest the user request
     * @return 함수 종료시, @AuthenticationPrincipal 어노테이션이 만들어진다!!
     * @throws OAuth2AuthenticationException
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("userRequest : " + userRequest);
        System.out.println("clientRegistration : " + userRequest.getClientRegistration());
        System.out.println("accessToken : " + userRequest.getAccessToken().getTokenValue());
        System.out.println("attributes : " + super.loadUser(userRequest).getAttributes());

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId(); // google

        OAuth2UserInfo oAuth2UserInfo = null;
        if (provider.equals("google")) {
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        } else if (provider.equals("kakao")) {
            oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
        } else if (provider.equals("naver")) {
            oAuth2UserInfo = new NaverUserInfo(oAuth2User.getAttribute("response"));
        } else {
            System.out.println("지원하지 않는 플랫폼입니다.");
        }

        String providerId = oAuth2UserInfo.getProviderId();
        String email = oAuth2UserInfo.getEmail();
        String username = provider + "_" + providerId;  // google_11028303829 이런 식으로 돼서, 겹칠 일 없음
        String password = null; // 큰 의미 없음.
        String role = "ROLE_USER";

        // 이미 회원가입 되어있으면 회원가입 안되게끔함.
        Member member = memberRepository.findByUsername(username);

        if (member == null) {
            member = Member.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .role(role)
                    .provider(provider)
                    .status(1)
                    .build();

            memberRepository.save(member);
        }
        //return super.loadUser(userRequest);
        return new PrincipalDetails(member, oAuth2User.getAttributes());
    }
}
