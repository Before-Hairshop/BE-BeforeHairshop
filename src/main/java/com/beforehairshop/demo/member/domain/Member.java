package com.beforehairshop.demo.member.domain;

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
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private BigInteger id;

    @Column(columnDefinition = "TEXT")
    private String email;

    @Column(columnDefinition = "VARCHAR(200) DEFAULT NULL")
    private String provider; // KAKAO, GOOGLE...

    private String username;
    private String password;
    private String role; // ROLE_USER, ROLE_DESIGNER, ROLE_ADMIN

    @Column(columnDefinition = "VARCHAR(250) DEFAULT 'tmp_name'")
    private String nickname;

    @Column(columnDefinition = "TEXT DEFAULT NULL")
    private String imageUrl;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private int designerFlag;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private int premiumFlag;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createDate;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updateDate;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private int status;


}
