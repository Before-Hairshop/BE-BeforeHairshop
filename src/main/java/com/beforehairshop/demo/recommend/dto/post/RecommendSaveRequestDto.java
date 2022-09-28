package com.beforehairshop.demo.recommend.dto.post;

import com.beforehairshop.demo.constant.member.StatusKind;
import com.beforehairshop.demo.constant.recommend.RecommendStatusKind;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.recommend.domain.Recommend;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendSaveRequestDto {
    private String greeting;

    @JsonFormat(pattern = "yyyy.MM.dd HH:mm")
    private Date treatmentDate;
    private String hairstyle;
    private String reason;
    private Integer price;


    public Recommend toEntity(Member recommender, Member recommendedPerson) {
        return Recommend.builder()
                .recommender(recommender)
                .recommendedPerson(recommendedPerson)
                .greeting(greeting)
                .treatmentDate(treatmentDate)
                .hairstyle(hairstyle)
                .reason(reason)
                .price(price)
                .recommendStatus(RecommendStatusKind.WAIT.getId())
                .status(StatusKind.NORMAL.getId())
                .build();
    }
}
