package com.beforehairshop.demo.member.repository;

import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface MemberProfileRepository extends JpaRepository<MemberProfile, BigInteger> {
    Optional<MemberProfile> findByMemberAndStatus(Member member, Integer status);

    @Query(value = "SELECT *, ( 6371 * acos (cos ( radians(?1) ) * cos( radians( m.latitude ) ) * cos( radians( m.longitude ) - radians(?2) ) + sin ( radians(?1) ) * sin( radians( m.latitude )))) AS distance " +
            "FROM member_profile m WHERE m.status = ?4 AND m.matching_activation_flag = 1 GROUP BY m.id HAVING distance <= 10 " +
            "ORDER BY distance asc LIMIT 5 OFFSET ?3 ;",
            nativeQuery = true)
    List<MemberProfile> findManyByLocationAndMatchingFlagAndStatus(Float designer_latitude, Float designer_longitude,  int pageOffset, Integer status);

    Optional<MemberProfile> findByIdAndStatus(BigInteger memberProfileId, Integer id);

}
