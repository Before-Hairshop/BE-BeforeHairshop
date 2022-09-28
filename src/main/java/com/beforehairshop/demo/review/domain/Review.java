package com.beforehairshop.demo.review.domain;

import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.review.dto.patch.ReviewPatchRequestDto;
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
@DynamicUpdate
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor
public class Review {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "BIGINT")
    private BigInteger id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member reviewer;

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

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createDate;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updateDate;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private int status;

}
