package com.beforehairshop.demo.hairdesigner.domain;

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
@NoArgsConstructor
@AllArgsConstructor
public class HairDesignerProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private BigInteger id;

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private String imageUrl;
    private String name;
    private String description;
    private String hairShopName;
    private Integer zipCode;  // 우편번호
    private String zipAddress;  // 우편번호에 해당하는 주소
    private Float latitude;  // 위도
    private Float longitude;  // 경도
    private String detailAddress;  // 상세주소
    private String phoneNumber;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createDate;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updateDate;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private int status;

}
