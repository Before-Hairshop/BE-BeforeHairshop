package com.beforehairshop.demo.hairdesigner.repository;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface HairDesignerProfileRepository extends JpaRepository<HairDesignerProfile, BigInteger> {
    Optional<HairDesignerProfile> findByMember(Member hairDesigner);

    @Query(value = "SELECT *, ( 6371 * acos (cos ( radians(?1) ) * cos( radians( h.latitude ) ) * cos( radians( h.longitude ) - radians(?2) ) + sin ( radians(?1) ) * sin( radians( h.latitude )))) AS distance " +
            "FROM hair_designer_profile h GROUP BY h.id HAVING distance <= 1.5 " +
            "ORDER BY distance LIMIT 5 OFFSET ?3 ;",
            nativeQuery = true)
    List<HairDesignerProfile> findManyByLocation(Float member_latitude, Float member_longitude, int pageOffset);
}
