package com.beforehairshop.demo.review.domain;

import com.beforehairshop.demo.hairdesigner.domain.HairDesigner;
import com.beforehairshop.demo.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Date;

@Entity
@Getter
@Builder
@DynamicUpdate
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor
public class Review {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private BigInteger id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToOne
    @JoinColumn(name = "hair_designer_id")
    private Member hairDesigner;

    @Column(columnDefinition = "TINYINT")
    private Integer totalRating;

    @Column(columnDefinition = "TINYINT")
    private Integer styleRating;

    @Column(columnDefinition = "TINYINT")
    private Integer serviceRating;

    @Column(columnDefinition = "TEXT DEFAULT NULL")
    private String content;

    @Column(columnDefinition = "TEXT DEFAULT NULL")
    private String virtualImageUrl;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createdAt;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updatedAt;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private int status;

}
