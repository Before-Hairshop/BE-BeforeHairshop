package com.beforehairshop.demo.member.dto;

import com.beforehairshop.demo.member.domain.Member;
import lombok.Getter;

import java.util.Date;

@Getter
public class MemberDto {
    private final Long id;
    private final String email;
    private final String socialLoginType; // KAKAO, GOOGLE...
    private final String name;
    private final String imageUrl;
    private final int designer_flag;
    private final int premium_flag;
    private final Date createdAt;
    private final Date updatedAt;
    private final int status;

    public MemberDto(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.socialLoginType = member.getSocialLoginType();
        this.name = member.getName();
        this.imageUrl = member.getImageUrl();
        this.designer_flag = member.getDesigner_flag();
        this.premium_flag = member.getPremium_flag();
        this.createdAt = member.getCreatedAt();
        this.updatedAt = member.getUpdatedAt();
        this.status = member.getStatus();
    }
}
