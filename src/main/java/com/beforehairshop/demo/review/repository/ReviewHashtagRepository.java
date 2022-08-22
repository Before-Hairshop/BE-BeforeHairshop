package com.beforehairshop.demo.review.repository;

import com.beforehairshop.demo.review.domain.ReviewHashtag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface ReviewHashtagRepository extends JpaRepository<ReviewHashtag, BigInteger> {
}
