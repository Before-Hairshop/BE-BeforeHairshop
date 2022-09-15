package com.beforehairshop.demo.hairdesigner.dto.patch;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerWorkingDay;
import com.beforehairshop.demo.member.domain.Member;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HairDesignerWorkingDayPatchRequestDto {
    private String workingDay;      // MON, TUE, WED ...
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
