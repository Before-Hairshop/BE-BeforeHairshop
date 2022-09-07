package com.beforehairshop.demo.member.dto;

import com.beforehairshop.demo.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@AllArgsConstructor
public class MemberSaveRequestDto {
    private String email;
    private String socialLoginType;
    private int status;

    // make member entity
    public Member toEntity() {
        return Member.builder()
                .email(email)
                .socialLoginType(socialLoginType)
                .status(status)
                .build();
    }
}
