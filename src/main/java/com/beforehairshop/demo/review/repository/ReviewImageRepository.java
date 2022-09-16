package com.beforehairshop.demo.review.repository;

import com.beforehairshop.demo.review.domain.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, BigInteger> {
}
