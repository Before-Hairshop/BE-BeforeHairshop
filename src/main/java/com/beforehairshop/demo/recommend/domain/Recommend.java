package com.beforehairshop.demo.recommend.domain;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfile;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Date;

@Entity
@Getter
@Setter
@Builder
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
public class Recommend {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "BIGINT")
    private BigInteger id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommender_id")
    private Member recommender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_person_id")
    private Member recommendedPerson;

    private String greeting;
    private Date treatmentDate;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createDate;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updateDate;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private int status;
}
