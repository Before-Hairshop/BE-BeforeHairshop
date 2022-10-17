package com.beforehairshop.demo.recommend.domain;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.member.domain.MemberProfile;
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
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
public class RecommendRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private BigInteger id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_recommend_request_profile_id")
    private HairDesignerProfile toRecommendRequestProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_recommend_request_profile_id")
    private MemberProfile fromRecommendRequestProfile;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createDate;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updateDate;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer status;

    public void setFromRecommendRequestProfile(MemberProfile memberProfile) {
        this.fromRecommendRequestProfile = memberProfile;
    }

    public void setToRecommendRequestProfile(HairDesignerProfile hairDesignerProfile) {
        this.toRecommendRequestProfile = hairDesignerProfile;
    }

    public RecommendRequest(HairDesignerProfile toRecommendRequestProfile, MemberProfile fromRecommendRequestProfile
            , Integer status) {
        this.toRecommendRequestProfile = toRecommendRequestProfile;
        this.fromRecommendRequestProfile = fromRecommendRequestProfile;
        this.status = status;
    }
}
