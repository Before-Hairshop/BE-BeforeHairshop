package com.beforehairshop.demo.hairdesigner.dto;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerWorkingDay;
import com.beforehairshop.demo.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigInteger;
import java.time.LocalTime;
import java.util.Date;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HairDesignerWorkingDayDto {
    private BigInteger id;
    private BigInteger hairDesignerId;
    private String workingDay;
    private LocalTime startTime;
    private LocalTime endTime;
    private Date createDate;
    private Date updateDate;
    private int status;

    public HairDesignerWorkingDayDto(HairDesignerWorkingDay hairDesignerWorkingDay) {
        this.id = hairDesignerWorkingDay.getId();
        this.hairDesignerId = hairDesignerWorkingDay.getHairDesigner().getId();
        this.workingDay = hairDesignerWorkingDay.getWorkingDay();
        this.startTime = hairDesignerWorkingDay.getStartTime();
        this.endTime = hairDesignerWorkingDay.getEndTime();
        this.createDate = hairDesignerWorkingDay.getCreateDate();
        this.updateDate = hairDesignerWorkingDay.getUpdateDate();
        this.status = hairDesignerWorkingDay.getStatus();
    }
}
