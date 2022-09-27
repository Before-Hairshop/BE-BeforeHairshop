package com.beforehairshop.demo.recommend.dto.post;

import com.beforehairshop.demo.constant.member.StatusKind;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.recommend.domain.Recommend;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendSaveRequestDto {
    private String greeting;
    private Date treatmentDate;
    private List<StyleRecommendSaveRequestDto> styleRecommendSaveRequestDtoList;

    public Recommend toEntity(Member recommender, Member recommendedPerson) {
        return Recommend.builder()
                .recommender(recommender)
                .recommendedPerson(recommendedPerson)
                .greeting(greeting)
                .treatmentDate(treatmentDate)
                .status(StatusKind.NORMAL.getId())
                .build();
    }
}
