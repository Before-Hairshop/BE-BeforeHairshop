package com.beforehairshop.demo.review.repository;

import com.beforehairshop.demo.review.domain.Review;
import com.beforehairshop.demo.review.domain.ReviewHashtag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface ReviewHashtagRepository extends JpaRepository<ReviewHashtag, BigInteger> {
    List<ReviewHashtag> findByReviewAndStatus(Review review, Integer status);

}
