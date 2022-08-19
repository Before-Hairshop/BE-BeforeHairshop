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
public class HairDesignerHashtag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private BigInteger id;

    @ManyToOne
    @JoinColumn(name = "hair_designer_id")
    private HairDesigner hairDesigner;

    private String tag;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createdAt;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updatedAt;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private int status;
}
