package com.beforehairshop.demo.member.dto.post;

import com.beforehairshop.demo.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class MemberSaveRequestDto {
    private String email;
    private String provider;
    private int status;

    // make member entity
    public Member toEntity() {
        return Member.builder()
                .email(email)
                .provider(provider)
                .status(status)
                .build();
    }
}
