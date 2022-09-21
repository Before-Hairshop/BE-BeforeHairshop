package com.beforehairshop.demo.member.dto;

import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfilePatchRequestDto {
    private String name;
    private Integer hairCondition;
    private Integer hairTendency;
    private String desiredHairstyle;
    private String desiredHairstyleDescription;

    private Integer payableAmount;
    private String zipCode;
    private String zipAddress;
    private Float latitude;
    private Float longitude;
    private String detailAddress;




}
