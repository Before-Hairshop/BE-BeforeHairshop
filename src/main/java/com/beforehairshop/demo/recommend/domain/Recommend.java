package com.beforehairshop.demo.recommend.domain;

import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.recommend.dto.patch.RecommendPatchRequestDto;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Date;

@Entity
@Getter
@Builder
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
public class Recommend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private BigInteger id;

//    @ManyToOne
//    @JoinColumn(name = "recommend_id")
//    private Recommend recommend;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommender_id")
    private Member recommender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_person_id")
    private Member recommendedPerson;

    private String greeting;
    private Date treatmentDate;

    private String hairstyle;
    private String reason;
    private Integer price;

    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer recommendStatus; // 수락(2), 대기중(1), 거절(0)

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createDate;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updateDate;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private int status;

    public void patchEntity(RecommendPatchRequestDto patchDto) {
        if (patchDto.getGreeting() != null)
            this.greeting = patchDto.getGreeting();

        if (patchDto.getHairstyle() != null)
            this.hairstyle = patchDto.getHairstyle();

        if (patchDto.getReason() != null)
            this.reason = patchDto.getReason();

        if (patchDto.getPrice() != null)
            this.price = patchDto.getPrice();
    }
}
