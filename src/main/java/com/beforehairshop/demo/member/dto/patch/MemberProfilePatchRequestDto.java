package com.beforehairshop.demo.member.dto.patch;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfilePatchRequestDto {
    private String name;
    private Integer hairCondition;
    private Integer hairTendency;
    private String desiredHairstyleDescription;

    private Integer payableAmount;
    private String zipCode;
    private String zipAddress;
    private Float latitude;
    private Float longitude;
    private Date treatmentDate;
    private String phoneNumber;

    private List<DesiredHairstylePatchRequestDto> desiredHairstyleList;



}
