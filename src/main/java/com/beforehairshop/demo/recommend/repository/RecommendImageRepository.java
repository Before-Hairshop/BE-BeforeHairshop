package com.beforehairshop.demo.recommend.repository;

import com.beforehairshop.demo.recommend.domain.Recommend;
import com.beforehairshop.demo.recommend.domain.RecommendImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface RecommendImageRepository extends JpaRepository<RecommendImage, BigInteger> {
    List<RecommendImage> findByRecommendAndStatus(Recommend recommend, Integer status);

    Optional<RecommendImage> findByImageUrlAndStatus(String imageUrl, Integer status);
}
