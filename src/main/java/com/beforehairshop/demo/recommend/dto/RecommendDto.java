package com.beforehairshop.demo.recommend.dto;

import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.recommend.domain.Recommend;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Date;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendDto {
    private BigInteger id;
    private BigInteger recommenderId;
    private BigInteger recommendedPersonId;
    private String greeting;
    private Date treatmentDate;
    private Date createDate;
    private Date updateDate;
    private int status;

    public RecommendDto(Recommend recommend) {
        this.id = recommend.getId();
        this.recommenderId = recommend.getRecommender().getId();
        this.recommendedPersonId = recommend.getRecommendedPerson().getId();
        this.greeting = recommend.getGreeting();
        this.treatmentDate = recommend.getTreatmentDate();
        this.createDate = recommend.getCreateDate();
        this.updateDate = recommend.getUpdateDate();
        this.status = recommend.getStatus();
    }

}
