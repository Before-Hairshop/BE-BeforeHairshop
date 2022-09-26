package com.beforehairshop.demo.recommend.repository;

import com.beforehairshop.demo.recommend.domain.StyleRecommend;
import com.beforehairshop.demo.recommend.domain.StyleRecommendImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface StyleRecommendImageRepository extends JpaRepository<StyleRecommendImage, BigInteger> {
    List<StyleRecommendImage> findByStyleRecommendAndStatus(StyleRecommend styleRecommend, Integer status);
}
