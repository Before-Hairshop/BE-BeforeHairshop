package com.beforehairshop.demo.hairdesigner.dto.response;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerHashtag;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerPrice;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerWorkingDay;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerHashtagDto;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerPriceDto;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerProfileDto;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerWorkingDayDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class HairDesignerDetailGetResponseDto {
    private HairDesignerProfileDto hairDesignerProfileDto;
    private Float averageStarRating;
    private List<HairDesignerHashtagDto> hairDesignerHashtagDtoList;
    private List<HairDesignerWorkingDayDto> hairDesignerWorkingDayDtoList;
    private List<HairDesignerPriceDto> hairDesignerPriceDtoList;
}
