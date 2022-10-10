package com.beforehairshop.demo.hairdesigner.domain;

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
public class HairDesignerPrice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private BigInteger id;

    @ManyToOne
    @JoinColumn(name = "hair_designer_profile_id")
    private HairDesignerProfile hairDesignerProfile;

    private String hairCategory;
    private String hairStyleName;
    private Integer price;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createDate;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updateDate;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer status;

    public HairDesignerPrice(HairDesignerProfile hairDesignerProfile, String hairCategory, String hairStyleName, Integer price, Integer status) {
        this.hairDesignerProfile = hairDesignerProfile;
        this.hairCategory = hairCategory;
        this.hairStyleName = hairStyleName;
        this.price = price;
        this.status = status;
    }

    public void setHairDesignerProfile(HairDesignerProfile hairDesignerProfile) {
        this.hairDesignerProfile = hairDesignerProfile;
    }
}
