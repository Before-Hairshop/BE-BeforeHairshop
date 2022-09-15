package com.beforehairshop.demo.hairdesigner.dto.post;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HairDesignerProfileSaveRequestDto {
    private String name;
    private String description;
    private String hairShopName;
    private String zipCode;  // 우편번호
    private String zipAddress;  // 우편번호에 해당하는 주소
    private Float latitude;  // 위도
    private Float longitude;  // 경도
    private String detailAddress;  // 상세주소
    private String phoneNumber;

    private List<HairDesignerHashtagSaveRequestDto> hashtagList;
    private List<HairDesignerWorkingDaySaveRequestDto> workingDayList;
    private List<HairDesignerPriceSaveRequestDto> priceList;

    public HairDesignerProfile toEntity(Member hairDesigner) {
        return HairDesignerProfile.builder()
                .hairDesigner(hairDesigner)
                .name(name)
                .imageUrl(null)
                .description(description)
                .hairShopName(hairShopName)
                .zipCode(zipCode)
                .zipAddress(zipAddress)
                .latitude(latitude)
                .longitude(longitude)
                .detailAddress(detailAddress)
                .phoneNumber(phoneNumber)
                .status(1)
                .build();
    }
}
