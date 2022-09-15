package com.beforehairshop.demo.hairdesigner.dto;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerHashtag;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerPrice;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerWorkingDay;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class HairDesignerDetailGetResponseDto {
    private HairDesignerProfile hairDesignerProfile;
    private List<HairDesignerHashtag> hairDesignerHashtagList;
    private List<HairDesignerWorkingDay> hairDesignerWorkingDayList;
    private List<HairDesignerPrice> hairDesignerPriceList;
}
