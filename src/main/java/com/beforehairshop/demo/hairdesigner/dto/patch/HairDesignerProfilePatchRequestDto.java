package com.beforehairshop.demo.hairdesigner.dto.patch;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerHashtag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HairDesignerProfilePatchRequestDto {
    private String name;
    private String description;
    private String hairShopName;
    private String zipCode;  // 우편번호
    private String zipAddress;  // 우편번호에 해당하는 주소
    private Float latitude;  // 위도
    private Float longitude;  // 경도
    private String detailAddress;  // 상세주소
    private String phoneNumber;

    private List<HairDesignerHashtagPatchRequestDto> hashtagPatchRequestDtoList;
    private List<HairDesignerPricePatchRequestDto> pricePatchRequestDtoList;
    private List<HairDesignerWorkingDayPatchRequestDto> workingDayPatchRequestDtoList;
}
