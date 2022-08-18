package com.beforehairshop.demo.hairdesigner.dto;

import com.beforehairshop.demo.hairdesigner.domain.HairDesigner;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerWorkingDay;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class HairDesignerWorkingDaySaveRequestDto {
    private String workingDay;
    private Date startTime;
    private Date endTime;


    public HairDesignerWorkingDay toEntity(HairDesigner hairDesigner) {
        return HairDesignerWorkingDay.builder()
                .hairDesigner(hairDesigner)
                .workingDay(workingDay)
                .startTime(startTime)
                .endTime(endTime)
                .status(1)
                .build();
    }
}
