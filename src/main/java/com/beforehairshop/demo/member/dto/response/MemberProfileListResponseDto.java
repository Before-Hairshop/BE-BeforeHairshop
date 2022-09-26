package com.beforehairshop.demo.member.dto.response;

import com.beforehairshop.demo.member.domain.MemberProfile;
import com.beforehairshop.demo.member.domain.MemberProfileDesiredHairstyle;
import com.beforehairshop.demo.member.dto.MemberProfileDesiredHairstyleDto;
import com.beforehairshop.demo.member.dto.MemberProfileDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileListResponseDto {
    private MemberProfileDto memberProfileDto;
    private List<MemberProfileDesiredHairstyleDto> memberProfileDesiredHairstyleDtoList;

}
