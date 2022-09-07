package com.beforehairshop.demo.member.dto;

import com.beforehairshop.demo.member.domain.Member;
import lombok.Getter;

import java.math.BigInteger;
import java.util.Date;

@Getter
public class MemberDto {
    private final BigInteger id;
    private final String email;
    private final String socialLoginType; // KAKAO, GOOGLE...
    private final String name;
    private final String imageUrl;
    private final int designerFlag;
    private final int premiumFlag;
    private final Date createDate;
    private final Date updateDate;
    private final int status;

    public MemberDto(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.socialLoginType = member.getProvider();
        this.name = member.getNickname();
        this.imageUrl = member.getImageUrl();
        this.designerFlag = member.getDesignerFlag();
        this.premiumFlag = member.getPremiumFlag();
        this.createDate = member.getCreateDate();
        this.updateDate = member.getUpdateDate();
        this.status = member.getStatus();
    }
}
