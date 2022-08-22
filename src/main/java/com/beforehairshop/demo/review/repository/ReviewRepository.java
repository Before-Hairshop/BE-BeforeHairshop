package com.beforehairshop.demo.review.repository;

import com.beforehairshop.demo.review.domain.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.math.BigInteger;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, BigInteger> {
    List<Review> findAllByHairDesignerId(BigInteger hairDesignerId, Pageable pageable);
}
