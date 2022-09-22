package com.beforehairshop.demo.member.dto;

import com.beforehairshop.demo.constant.StatusKind;
import com.beforehairshop.demo.member.domain.MemberProfile;
import com.beforehairshop.demo.member.domain.MemberProfileDesiredHairstyle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DesiredHairstyleSaveRequestDto {
    private String desiredHairstyle;

    public MemberProfileDesiredHairstyle toEntity(MemberProfile memberProfile) {
        return MemberProfileDesiredHairstyle.builder()
                .memberProfile(memberProfile)
                .desiredHairstyle(desiredHairstyle)
                .status(StatusKind.NORMAL.getId())
                .build();
    }
}
