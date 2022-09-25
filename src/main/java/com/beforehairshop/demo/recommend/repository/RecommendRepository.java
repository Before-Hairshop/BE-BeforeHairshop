package com.beforehairshop.demo.recommend.repository;

import com.beforehairshop.demo.recommend.domain.Recommend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface RecommendRepository extends JpaRepository<Recommend, BigInteger> {
}
