package com.beforehairshop.demo.recommend.repository;

import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.recommend.domain.Recommend;
import com.beforehairshop.demo.review.domain.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface RecommendRepository extends JpaRepository<Recommend, BigInteger> {

    Optional<Recommend> findByIdAndStatus(BigInteger bigInteger, Integer status);

    List<Recommend> findByRecommendedPersonAndStatus(Member recommendedPerson, Integer Status, Pageable pageable);
}
