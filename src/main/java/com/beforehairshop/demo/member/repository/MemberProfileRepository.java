package com.beforehairshop.demo.member.repository;

import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface MemberProfileRepository extends JpaRepository<MemberProfile, BigInteger> {
    Optional<MemberProfile> findByMemberAndStatus(Member member, Integer status);
}
