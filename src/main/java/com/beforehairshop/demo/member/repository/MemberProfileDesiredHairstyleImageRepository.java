package com.beforehairshop.demo.member.repository;

import com.beforehairshop.demo.member.domain.MemberProfileDesiredHairstyleImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface MemberProfileDesiredHairstyleImageRepository extends JpaRepository<MemberProfileDesiredHairstyleImage, BigInteger> {

    Optional<MemberProfileDesiredHairstyleImage> findByImageUrlAndStatus(String imageUrl, Integer status);

}
