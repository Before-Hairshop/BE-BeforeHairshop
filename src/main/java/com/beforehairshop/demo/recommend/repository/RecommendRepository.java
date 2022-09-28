package com.beforehairshop.demo.recommend.repository;

import com.beforehairshop.demo.recommend.domain.Recommend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface RecommendRepository extends JpaRepository<Recommend, BigInteger> {

    Optional<Recommend> findByIdAndStatus(BigInteger bigInteger, Integer status);
}
