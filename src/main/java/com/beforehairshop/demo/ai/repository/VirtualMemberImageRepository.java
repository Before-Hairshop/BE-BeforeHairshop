package com.beforehairshop.demo.ai.repository;

import com.beforehairshop.demo.ai.domain.VirtualMemberImage;
import com.beforehairshop.demo.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface VirtualMemberImageRepository extends JpaRepository<VirtualMemberImage, BigInteger> {
    Optional<VirtualMemberImage> findByIdAndInferenceStatus(BigInteger id, Integer inferenceStatus);

    Optional<VirtualMemberImage> findByImageUrlAndStatus(String virtualMemberImageUrl, Integer status);

    List<VirtualMemberImage> findByMemberAndStatus(Member member, Integer status);

    List<VirtualMemberImage> findByMemberAndStatusOrderByCreateDateAsc(Member member, Integer status);
}
