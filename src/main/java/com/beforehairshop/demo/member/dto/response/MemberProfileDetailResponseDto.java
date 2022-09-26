package com.beforehairshop.demo.member.dto.response;

import com.beforehairshop.demo.member.domain.MemberProfile;
import com.beforehairshop.demo.member.domain.MemberProfileDesiredHairstyle;
import com.beforehairshop.demo.member.domain.MemberProfileDesiredHairstyleImage;
import com.beforehairshop.demo.member.dto.MemberProfileDesiredHairstyleDto;
import com.beforehairshop.demo.member.dto.MemberProfileDesiredHairstyleImageDto;
import com.beforehairshop.demo.member.dto.MemberProfileDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MemberProfileDetailResponseDto {
    private MemberProfileDto memberProfileDto;
    private List<MemberProfileDesiredHairstyleDto> desiredHairstyleDtoList;
    private List<MemberProfileDesiredHairstyleImageDto> desiredHairstyleImageDtoList;
}
