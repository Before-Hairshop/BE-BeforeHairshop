package com.beforehairshop.demo.hairdesigner.dto;

import com.beforehairshop.demo.hairdesigner.domain.HairDesigner;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerWorkingDay;
import com.beforehairshop.demo.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class HairDesignerSaveRequestDto {
    private Long memberId;
    private String description;
    private String zipCode;
    private String detailAddress;
    private String phoneNumber;
    private int status;

    private List<HairDesignerWorkingDaySaveRequestDto> hairDesignerWorkingDaySaveRequestDtoList;

    public HairDesigner toEntity(Member member) {
        return HairDesigner.builder()
                .member(member)
                .description(description)
                .zipCode(zipCode)
                .detailAddress(detailAddress)
                .phoneNumber(phoneNumber)
                .status(status)
                .build();
    }
}
