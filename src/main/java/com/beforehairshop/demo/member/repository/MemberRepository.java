package com.beforehairshop.demo.member.repository;

import com.beforehairshop.demo.member.domain.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findOneByEmailAndStatusIsLessThan(String email, int status);
}
