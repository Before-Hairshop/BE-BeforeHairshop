package com.beforehairshop.demo.hairdesigner.dto;

import com.beforehairshop.demo.hairdesigner.domain.HairDesigner;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerWorkingDay;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class HairDesignerDetailResponseDto {
    private HairDesigner hairDesigner;
    private List<HairDesignerWorkingDay> hairDesignerWorkingDayList;
}
