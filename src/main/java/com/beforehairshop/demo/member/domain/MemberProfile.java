package com.beforehairshop.demo.member.domain;

import com.beforehairshop.demo.member.dto.MemberProfileDto;
import com.beforehairshop.demo.member.dto.post.MemberProfileSaveRequestDto;
import com.beforehairshop.demo.recommend.domain.Recommend;
import com.beforehairshop.demo.recommend.domain.RecommendRequest;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.lang.Nullable;

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
public class MemberProfile {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private BigInteger id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String name;

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
    private String zipCode;
    private String zipAddress;
    private Float latitude;
    private Float longitude;

    private String phoneNumber;
    private Date treatmentDate;

    @Column(columnDefinition = "TINYINT DEFAULT 1") // 1 (활성화), 0 (비활성화)
    private Integer matchingActivationFlag;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createDate;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updateDate;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private int status;

    @OneToMany(mappedBy = "recommendedProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Recommend> recommendedSet = new HashSet<>();

    @OneToMany(mappedBy = "memberProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MemberProfileDesiredHairstyleImage> memberProfileDesiredHairstyleImageSet = new HashSet<>();

    @OneToMany(mappedBy = "fromRecommendRequestProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RecommendRequest> fromRecommendRequestSet = new HashSet<>();


    public void addFromRecommendRequest(RecommendRequest recommendRequest) {
        this.fromRecommendRequestSet.add(recommendRequest);
        recommendRequest.setFromRecommendRequestProfile(this);
    }


    public void addDesiredHairstyleImage(MemberProfileDesiredHairstyleImage desiredHairstyleImage) {
        this.memberProfileDesiredHairstyleImageSet.add(desiredHairstyleImage);
        desiredHairstyleImage.setMemberProfile(this);
    }

    public MemberProfile(Member member, MemberProfileSaveRequestDto saveRequestDto, String frontImageUrl, String sideImageUrl, String backImageUrl, Integer matchingFlag, Integer status) {
        this.member = member;
        this.name = saveRequestDto.getName();
        this.hairCondition = saveRequestDto.getHairCondition();
        this.hairTendency = saveRequestDto.getHairTendency();
        this.desiredHairstyle = saveRequestDto.getDesiredHairstyle();
        this.desiredHairstyleDescription = saveRequestDto.getDesiredHairstyleDescription();
        this.frontImageUrl = frontImageUrl;
        this.sideImageUrl = sideImageUrl;
        this.backImageUrl = backImageUrl;
        this.payableAmount = saveRequestDto.getPayableAmount();
        this.zipCode = saveRequestDto.getZipCode();
        this.zipAddress = saveRequestDto.getZipAddress();
        this.latitude = saveRequestDto.getLatitude();
        this.longitude = saveRequestDto.getLongitude();
        this.phoneNumber = saveRequestDto.getPhoneNumber();
        this.treatmentDate = saveRequestDto.getTreatmentDate();
        this.matchingActivationFlag = matchingFlag;
        this.status = status;
    }
}
