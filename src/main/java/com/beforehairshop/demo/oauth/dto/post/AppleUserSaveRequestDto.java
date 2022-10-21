package com.beforehairshop.demo.oauth.dto.post;

import com.beforehairshop.demo.constant.member.StatusKind;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.oauth.helper.constants.SocialLoginType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AppleUserSaveRequestDto {
    private String providerId;
    private String email;
    private String accessToken;

    public Member toEntity(String username) {
        return Member.builder()
                .name("임시 USER")
                .email(email)
                .provider(SocialLoginType.APPLE.getProvider())
                .username(username)
                .designerFlag(0)
                .premiumFlag(0)
                .status(StatusKind.ABNORMAL.getId())
                .role("ROLE_USER")
                .build();
    }
}
