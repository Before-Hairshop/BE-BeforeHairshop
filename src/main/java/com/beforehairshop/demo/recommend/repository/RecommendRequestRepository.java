package com.beforehairshop.demo.recommend.repository;

import com.beforehairshop.demo.recommend.domain.RecommendRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface RecommendRequestRepository extends JpaRepository<RecommendRequest, BigInteger> {
    Optional<RecommendRequest> findByIdAndStatus(BigInteger id, Integer status);
}
