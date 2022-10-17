package com.beforehairshop.demo.recommend.repository;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.recommend.domain.Recommend;
import com.beforehairshop.demo.review.domain.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface RecommendRepository extends JpaRepository<Recommend, BigInteger> {

    Optional<Recommend> findByIdAndStatus(BigInteger bigInteger, Integer status);

    @Query(value = "select r.*, ( 6371 * acos (cos ( radians(?2) ) * cos( radians( h.latitude ) ) * cos( radians( h.longitude ) - radians(?3) ) + sin ( radians(?2) ) * sin( radians( h.latitude )))) AS distance " +
            "from recommend r, hair_designer_profile h " +
            "where r.recommender_profile_id = h.id and r.recommended_profile_id = ?1 and r.status = ?4 " +
            "group by r.id HAVING distance <= 10 " +
            "ORDER BY distance asc ;",
            nativeQuery = true)
    List<Recommend> findByRecommendedProfileAndStatusAndSortingByLocation(BigInteger recommendedProfileId, Float member_latitude, Float member_longitude, Integer Status);

    List<Recommend> findByRecommenderProfileAndStatusOrderByCreateDate(HairDesignerProfile recommenderProfile, Integer status);
}
