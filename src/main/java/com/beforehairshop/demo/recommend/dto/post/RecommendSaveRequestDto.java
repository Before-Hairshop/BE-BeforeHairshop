package com.beforehairshop.demo.recommend.dto.post;

import com.beforehairshop.demo.constant.member.StatusKind;
import com.beforehairshop.demo.constant.recommend.RecommendStatusKind;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfile;
import com.beforehairshop.demo.recommend.domain.Recommend;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendSaveRequestDto {
    private String greeting;


    private String hairstyle;
    private String reason;
    private Integer price;


    public Recommend toEntity(HairDesignerProfile recommenderProfile
            , MemberProfile recommendedProfile) {
        return Recommend.builder()
                .recommenderProfile(recommenderProfile)
                .recommendedProfile(recommendedProfile)
                .greeting(greeting)
                .treatmentDate(recommendedProfile.getTreatmentDate())
                .hairstyle(hairstyle)
                .reason(reason)
                .price(price)
                .recommendStatus(RecommendStatusKind.WAIT.getId())
                .status(StatusKind.NORMAL.getId())
                .build();
    }
}
