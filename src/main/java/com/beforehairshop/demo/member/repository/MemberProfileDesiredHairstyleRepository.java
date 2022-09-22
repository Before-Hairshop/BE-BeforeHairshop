package com.beforehairshop.demo.member.repository;

import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfile;
import com.beforehairshop.demo.member.domain.MemberProfileDesiredHairstyle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface MemberProfileDesiredHairstyleRepository extends JpaRepository<MemberProfileDesiredHairstyle, BigInteger> {

    List<MemberProfileDesiredHairstyle> findByMemberProfileAndStatus(MemberProfile memberProfile, Integer status);
}
