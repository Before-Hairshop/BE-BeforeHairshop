package com.beforehairshop.demo.hairdesigner.repository;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface HairDesignerRepository extends JpaRepository<HairDesignerProfile, BigInteger> {
    Optional<HairDesignerProfile> findByMember(Member hairDesigner);
}
