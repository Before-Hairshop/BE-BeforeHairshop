package com.beforehairshop.demo.review.repository;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.review.domain.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, BigInteger> {
    Optional<Review> findByIdAndStatus(BigInteger id, Integer status);
    List<Review> findAllByHairDesignerProfileIdAndStatus(BigInteger hairDesignerProfileId, Integer Status, Pageable pageable);

    @Query(value = "SELECT ROUND(AVG(total_rating) , 1) FROM REVIEW \n" +
            "WHERE hair_designer_id = ?1 AND status = ?2 \n" +
            "GROUP BY hair_designer_id;"
            , nativeQuery = true)
    Float calculateByHairDesignerIdAndStatus(BigInteger hairDesignerId, Integer status);
}
