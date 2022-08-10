package com.beforehairshop.demo.member.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Builder
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String email;

    @Column(columnDefinition = "VARCHAR(200) DEFAULT NULL")
    private String socialLoginType; // KAKAO, GOOGLE...

    @Column(columnDefinition = "VARCHAR(250) DEFAULT 'tmp_name'")
    private String name;

    @Column(columnDefinition = "TEXT DEFAULT NULL")
    private String imageUrl;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private int designer_flag;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private int premium_flag;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createdAt;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updatedAt;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private int status;


}
