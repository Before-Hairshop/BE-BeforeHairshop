package com.beforehairshop.demo.hairdesigner.repository;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface HairDesignerProfileRepository extends JpaRepository<HairDesignerProfile, BigInteger> {

    Optional<HairDesignerProfile> findByHairDesignerAndStatus(Member hairDesigner, Integer status);

    List<HairDesignerProfile> findAllByNameAndStatus(String name, Integer status, Pageable pageable);

    @Query(value = "SELECT *, ( 6371 * acos (cos ( radians(?1) ) * cos( radians( h.latitude ) ) * cos( radians( h.longitude ) - radians(?2) ) + sin ( radians(?1) ) * sin( radians( h.latitude )))) AS distance " +
            "FROM hair_designer_profile h WHERE h.status = ?4 GROUP BY h.id HAVING distance <= 10 " +
            "ORDER BY distance asc LIMIT 5 OFFSET ?3 ;",
            nativeQuery = true)
    List<HairDesignerProfile> findManyByLocationAndStatus(Float member_latitude, Float member_longitude, int pageOffset, Integer status);
}
