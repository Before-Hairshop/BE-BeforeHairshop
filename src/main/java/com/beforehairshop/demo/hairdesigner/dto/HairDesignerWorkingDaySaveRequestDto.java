package com.beforehairshop.demo.hairdesigner.dto;

import com.beforehairshop.demo.hairdesigner.domain.HairDesigner;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerWorkingDay;
import com.beforehairshop.demo.member.domain.Member;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;
import java.util.Date;

@Getter
@AllArgsConstructor
public class HairDesignerWorkingDaySaveRequestDto {
    private String workingDay;
    @JsonFormat(pattern = "kk:mm:ss")
    private LocalTime startTime;
    @JsonFormat(pattern = "kk:mm:ss")
    private LocalTime endTime;


    public HairDesignerWorkingDay toEntity(Member hairDesigner) {
        return HairDesignerWorkingDay.builder()
                .hairDesigner(hairDesigner)
                .workingDay(workingDay)
                .startTime(startTime)
                .endTime(endTime)
                .status(1)
                .build();
    }
}
