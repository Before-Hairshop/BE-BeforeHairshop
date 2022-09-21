package com.beforehairshop.demo.member.dto;

import com.beforehairshop.demo.member.domain.MemberProfile;
import com.beforehairshop.demo.member.domain.MemberProfileDesiredHairstyleImage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MemberProfileDetailResponseDto {
    private MemberProfile memberProfile;
    private List<MemberProfileDesiredHairstyleImage> desiredHairstyleImageList;
}
