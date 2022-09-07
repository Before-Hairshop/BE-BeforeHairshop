package com.beforehairshop.demo.member.repository;

import com.beforehairshop.demo.member.domain.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, BigInteger> {

    Optional<Member> findOneByEmailAndStatusIsLessThan(String email, int status);
    Member findByUsername(String username);
}
