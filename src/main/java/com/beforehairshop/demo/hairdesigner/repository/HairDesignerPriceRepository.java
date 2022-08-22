package com.beforehairshop.demo.hairdesigner.repository;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface HairDesignerPriceRepository extends JpaRepository<HairDesignerPrice, BigInteger> {
}
