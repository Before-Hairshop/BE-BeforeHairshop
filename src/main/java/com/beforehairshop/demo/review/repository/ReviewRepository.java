package com.beforehairshop.demo.review.repository;

import com.beforehairshop.demo.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface ReviewRepository extends JpaRepository<Review, BigInteger> {
}
