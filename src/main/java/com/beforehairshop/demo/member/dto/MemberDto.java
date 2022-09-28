package com.beforehairshop.demo.member.dto;

import com.beforehairshop.demo.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.Date;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {
    private BigInteger id;
    private String email;
    private String role;
    private String socialLoginType; // KAKAO, GOOGLE...
    private String name;
    private String imageUrl;
    private int designerFlag;
    private int premiumFlag;
    private Date createDate;
    private Date updateDate;
    private int status;

    public MemberDto(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.role = member.getRole();
        this.socialLoginType = member.getProvider();
        this.name = member.getName();
        this.imageUrl = member.getImageUrl();
        this.designerFlag = member.getDesignerFlag();
        this.premiumFlag = member.getPremiumFlag();
        this.createDate = member.getCreateDate();
        this.updateDate = member.getUpdateDate();
        this.status = member.getStatus();
    }
}
