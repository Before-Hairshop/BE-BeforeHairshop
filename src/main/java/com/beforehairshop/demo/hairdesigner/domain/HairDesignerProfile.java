package com.beforehairshop.demo.hairdesigner.domain;

import com.beforehairshop.demo.hairdesigner.dto.post.HairDesignerProfileSaveRequestDto;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.recommend.domain.Recommend;
import com.beforehairshop.demo.recommend.domain.RecommendRequest;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
public class HairDesignerProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private BigInteger id;

    @ManyToOne
    @JoinColumn(name = "hair_designer_id")
    private Member hairDesigner;

    private String imageUrl;
    private String name;
    private String description;
    private String hairShopName;
    private String zipCode;  // 우편번호
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


    @OneToMany(mappedBy = "recommenderProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Recommend> recommendSet = new HashSet<>();


    @OneToMany(mappedBy = "hairDesignerProfile"
            , cascade = CascadeType.ALL
            , orphanRemoval = true)
    Set<HairDesignerPrice> hairDesignerPriceSet = new HashSet<>();

    @OneToMany(mappedBy = "hairDesignerProfile"
            , cascade = CascadeType.ALL
            , orphanRemoval = true)
    Set<HairDesignerHashtag> hairDesignerHashtagSet = new HashSet<>();

    @OneToMany(mappedBy = "hairDesignerProfile"
            , cascade = CascadeType.ALL
            , orphanRemoval = true)
    Set<HairDesignerWorkingDay> hairDesignerWorkingDaySet = new HashSet<>();

    @OneToMany(mappedBy = "toRecommendRequestProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    Set<RecommendRequest> toRecommendRequestSet = new HashSet<>();

    public void addToRecommendRequest(RecommendRequest recommendRequest) {
        this.toRecommendRequestSet.add(recommendRequest);
        recommendRequest.setToRecommendRequestProfile(this);
    }

    public void addHashtag(HairDesignerHashtag hashtag) {
        this.hairDesignerHashtagSet.add(hashtag);
        hashtag.setHairDesignerProfile(this);
    }

    public void addPrice(HairDesignerPrice price) {
        this.hairDesignerPriceSet.add(price);
        price.setHairDesignerProfile(this);
    }

    public void addWorkingDay(HairDesignerWorkingDay workingDay) {
        this.hairDesignerWorkingDaySet.add(workingDay);
        workingDay.setHairDesignerProfile(this);
    }


    public HairDesignerProfile (Member hairDesigner, HairDesignerProfileSaveRequestDto saveRequestDto, Integer status) {
        this.hairDesigner = hairDesigner;
        this.imageUrl = null;
        this.name = saveRequestDto.getName();
        this.description = saveRequestDto.getDescription();
        this.hairShopName = saveRequestDto.getHairShopName();
        this.zipCode = saveRequestDto.getZipCode();
        this.zipAddress = saveRequestDto.getZipAddress();
        this.latitude = saveRequestDto.getLatitude();
        this.longitude = saveRequestDto.getLongitude();
        this.detailAddress = saveRequestDto.getDetailAddress();
        this.phoneNumber = saveRequestDto.getPhoneNumber();
        this.status = status;
    }
}
