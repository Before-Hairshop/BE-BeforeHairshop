package com.beforehairshop.demo.hairdesigner.dto;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerHashtag;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigInteger;
import java.util.List;

@Getter
@AllArgsConstructor
public class HairDesignerProfileSaveRequestDto {
    private String description;
    private String zipCode;
    private String detailAddress;
    private String phoneNumber;

    private List<HairDesignerHashtagSaveRequestDto> hashtagList;
    private List<HairDesignerWorkingDaySaveRequestDto> workingDayList;
    private List<HairDesignerPriceSaveRequestDto> priceList;

    public HairDesignerProfile toEntity(Member hairDesigner) {
        return HairDesignerProfile.builder()
                .member(hairDesigner)
                .description(description)
                .zipCode(zipCode)
                .detailAddress(detailAddress)
                .phoneNumber(phoneNumber)
                .status(1)
                .build();
    }
}
