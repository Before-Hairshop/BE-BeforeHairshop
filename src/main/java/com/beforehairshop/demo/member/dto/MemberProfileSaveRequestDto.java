package com.beforehairshop.demo.member.dto;

import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberProfileSaveRequestDto {

    private Integer hairCondition;
    private Integer hairTendency;
    private String desiredHairstyle;
    private String desiredHairstyleDescription;
//    private String frontImageUrl;
//    private String sideImageUrl;
//    private String backImageUrl;
//
//    public MemberProfile toEntity(Member member) {
//
//    }
}
