package com.beforehairshop.demo.recommend.repository;

import com.beforehairshop.demo.recommend.domain.StyleRecommendImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface StyleRecommendImageRepository extends JpaRepository<StyleRecommendImage, BigInteger> {
}
