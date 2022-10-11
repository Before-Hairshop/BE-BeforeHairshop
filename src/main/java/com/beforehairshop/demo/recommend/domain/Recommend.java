package com.beforehairshop.demo.recommend.domain;

import com.beforehairshop.demo.constant.recommend.RecommendStatusKind;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfile;
import com.beforehairshop.demo.recommend.dto.patch.RecommendPatchRequestDto;
import com.beforehairshop.demo.recommend.dto.post.RecommendSaveRequestDto;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Builder
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
public class Recommend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private BigInteger id;

//    @ManyToOne
//    @JoinColumn(name = "recommend_id")
//    private Recommend recommend;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommender_profile_id")
    private HairDesignerProfile recommenderProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_profile_id")
    private MemberProfile recommendedProfile;

    private String greeting;
    private Date treatmentDate;

    private String hairstyle;
    private String reason;
    private Integer price;

    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer recommendStatus; // 수락(2), 대기중(1), 거절(0)

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createDate;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updateDate;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private int status;

    @OneToMany(mappedBy = "recommend", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<RecommendImage> recommendImageSet = new HashSet<>();

    public Recommend(HairDesignerProfile recommenderProfile
            , MemberProfile recommendedProfile
            , RecommendSaveRequestDto saveRequestDto
            , Integer status) {
        this.recommenderProfile = recommenderProfile;
        this.recommendedProfile = recommendedProfile;
        this.greeting = saveRequestDto.getGreeting();
        this.treatmentDate = recommendedProfile.getTreatmentDate();
        this.hairstyle = saveRequestDto.getHairstyle();
        this.reason = saveRequestDto.getReason();
        this.price = saveRequestDto.getPrice();
        this.recommendStatus = RecommendStatusKind.WAIT.getId();
        this.status = status;
    }

    public void patchEntity(RecommendPatchRequestDto patchDto) {
        if (patchDto.getGreeting() != null)
            this.greeting = patchDto.getGreeting();

        if (patchDto.getHairstyle() != null)
            this.hairstyle = patchDto.getHairstyle();

        if (patchDto.getReason() != null)
            this.reason = patchDto.getReason();

        if (patchDto.getPrice() != null)
            this.price = patchDto.getPrice();
    }

    public void acceptRecommend() {
        this.recommendStatus = RecommendStatusKind.ACCEPT.getId();
    }

    public void rejectRecommend() {
        this.recommendStatus = RecommendStatusKind.REJECT.getId();
    }

    public void addRecommendImage(RecommendImage recommendImage) {
        this.recommendImageSet.add(recommendImage);
        recommendImage.setRecommend(this);
    }

}
