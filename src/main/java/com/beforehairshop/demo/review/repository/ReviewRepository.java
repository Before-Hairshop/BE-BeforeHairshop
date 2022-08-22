package com.beforehairshop.demo.review.repository;

import com.beforehairshop.demo.review.domain.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.math.BigInteger;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, BigInteger> {
    List<Review> findAllByHairDesignerId(BigInteger hairDesignerId, Pageable pageable);
}
