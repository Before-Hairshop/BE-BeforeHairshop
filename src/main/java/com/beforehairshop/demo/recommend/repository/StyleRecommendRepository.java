package com.beforehairshop.demo.recommend.repository;

import com.beforehairshop.demo.recommend.domain.StyleRecommend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface StyleRecommendRepository extends JpaRepository<StyleRecommend, BigInteger> {

    Optional<StyleRecommend> findByIdAndStatus(BigInteger bigInteger, Integer status);
}
