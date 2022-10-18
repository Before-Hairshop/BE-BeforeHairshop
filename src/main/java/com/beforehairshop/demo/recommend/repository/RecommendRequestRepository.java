package com.beforehairshop.demo.recommend.repository;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.member.domain.MemberProfile;
import com.beforehairshop.demo.recommend.domain.RecommendRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface RecommendRequestRepository extends JpaRepository<RecommendRequest, BigInteger> {
    Optional<RecommendRequest> findByIdAndStatus(BigInteger id, Integer status);

    Optional<RecommendRequest> findByToRecommendRequestProfileAndFromRecommendRequestProfileAndStatus(
            HairDesignerProfile toRecommendRequestProfile
            , MemberProfile fromRecommendRequestProfile
            , Integer status
    );

    List<RecommendRequest> findByToRecommendRequestProfileAndStatusOrderByCreateDateDesc(HairDesignerProfile toRecommendRequestProfile, Integer status);


    List<RecommendRequest> findByFromRecommendRequestProfileAndStatus(MemberProfile memberProfile, Integer id);

}
