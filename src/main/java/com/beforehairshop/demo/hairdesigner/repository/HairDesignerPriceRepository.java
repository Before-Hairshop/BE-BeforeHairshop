package com.beforehairshop.demo.hairdesigner.repository;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerPrice;
import com.beforehairshop.demo.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface HairDesignerPriceRepository extends JpaRepository<HairDesignerPrice, BigInteger> {
    List<HairDesignerPrice> findAllByHairDesigner(Member hairDesigner);
}
