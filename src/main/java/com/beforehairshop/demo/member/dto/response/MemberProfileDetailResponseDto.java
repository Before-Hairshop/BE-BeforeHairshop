package com.beforehairshop.demo.member.dto.response;

import com.beforehairshop.demo.member.dto.MemberProfileDesiredHairstyleImageDto;
import com.beforehairshop.demo.member.dto.MemberProfileDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MemberProfileDetailResponseDto {
    private MemberProfileDto memberProfileDto;
    private List<MemberProfileDesiredHairstyleImageDto> desiredHairstyleImageDtoList;
}
