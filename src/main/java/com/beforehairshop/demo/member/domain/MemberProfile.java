package com.beforehairshop.demo.member.domain;

import com.beforehairshop.demo.member.dto.MemberProfilePatchRequestDto;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Date;

@Entity
@Getter
@Setter
@Builder
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfile {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private BigInteger id;

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(columnDefinition = "TINYINT")
    private Integer hairCondition;

    @Column(columnDefinition = "TINYINT")
    private Integer hairTendency;

    private String desiredHairstyle;
    private String desiredHairstyleDescription;

    @Column(columnDefinition = "TEXT")
    private String frontImageUrl;

    @Column(columnDefinition = "TEXT DEFAULT NULL") @Nullable
    private String sideImageUrl;

    @Column(columnDefinition = "TEXT DEFAULT NULL") @Nullable
    private String backImageUrl;

    private Integer payableAmount;
    private Integer zipCode;
    private String zipAddress;
    private Float latitude;
    private Float longitude;
    private String detailAddress;
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createDate;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updateDate;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private int status;



}
