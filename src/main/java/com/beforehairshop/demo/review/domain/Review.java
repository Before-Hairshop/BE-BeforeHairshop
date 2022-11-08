package com.beforehairshop.demo.review.domain;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerPrice;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.review.dto.patch.ReviewPatchRequestDto;
import com.beforehairshop.demo.review.dto.save.ReviewSaveRequestDto;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;
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
@AllArgsConstructor
@NoArgsConstructor
public class Review {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private BigInteger id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member reviewer;

    @ManyToOne
    @JoinColumn(name = "hair_designer_profile_id")
    private HairDesignerProfile hairDesignerProfile;

    @Column(columnDefinition = "TINYINT")
    private Integer totalRating;

    @Column(columnDefinition = "TINYINT")
    private Integer styleRating;

    @Column(columnDefinition = "TINYINT")
    private Integer serviceRating;

    @Column(columnDefinition = "TEXT DEFAULT NULL")
    private String content;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createDate;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updateDate;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private int status;

    @OneToMany(mappedBy = "review"
            , fetch = FetchType.LAZY
            , cascade = CascadeType.ALL
            , orphanRemoval = true)
    private Set<ReviewHashtag> reviewHashtagSet = new HashSet<>();

    @OneToMany(mappedBy = "review"
            , fetch = FetchType.LAZY
            , cascade = CascadeType.ALL
            , orphanRemoval = true)
    private Set<ReviewImage> reviewImageSet = new HashSet<>();

    public void addReviewHashtag(ReviewHashtag reviewHashtag) {
        this.reviewHashtagSet.add(reviewHashtag);
        reviewHashtag.setReview(this);
    }

    public void addReviewImage(ReviewImage reviewImage) {
        this.reviewImageSet.add(reviewImage);
        reviewImage.setReview(this);
    }

    public Review(ReviewSaveRequestDto saveRequestDto, Member reviewer, HairDesignerProfile hairDesignerProfile, Integer status) {
        this.reviewer = reviewer;
        this.hairDesignerProfile = hairDesignerProfile;
        this.totalRating = saveRequestDto.getTotalRating();
        this.styleRating = saveRequestDto.getStyleRating();
        this.serviceRating = saveRequestDto.getServiceRating();
        this.content = saveRequestDto.getContent();
        this.status = status;
    }
}
