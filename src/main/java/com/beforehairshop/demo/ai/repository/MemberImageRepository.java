package com.beforehairshop.demo.ai.repository;

import com.beforehairshop.demo.ai.domain.MemberImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface MemberImageRepository extends JpaRepository<MemberImage, BigInteger> {
    Optional<MemberImage> findByIdAndInferenceStatus(BigInteger id, Integer inferenceStatus);
}
