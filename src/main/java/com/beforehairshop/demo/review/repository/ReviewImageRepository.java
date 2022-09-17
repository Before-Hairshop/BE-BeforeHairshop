package com.beforehairshop.demo.review.repository;

import com.beforehairshop.demo.review.domain.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, BigInteger> {
    Optional<ReviewImage> findByIdAndStatus(BigInteger id, Integer status);
}
