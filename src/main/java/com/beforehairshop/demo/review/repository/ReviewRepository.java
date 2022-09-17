package com.beforehairshop.demo.review.repository;

import com.beforehairshop.demo.review.domain.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, BigInteger> {
    Optional<Review> findByIdAndStatus(BigInteger id, Integer status);
    List<Review> findAllByHairDesignerIdAndStatus(BigInteger hairDesignerId, Integer Status, Pageable pageable);
}
