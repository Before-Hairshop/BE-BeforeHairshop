package com.beforehairshop.demo.ai.repository;

import com.beforehairshop.demo.ai.domain.VirtualMemberImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface VirtualMemberImageRepository extends JpaRepository<VirtualMemberImage, BigInteger> {
    Optional<VirtualMemberImage> findByIdAndInferenceStatus(BigInteger id, Integer inferenceStatus);
}
