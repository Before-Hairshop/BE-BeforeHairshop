package com.beforehairshop.demo.hairdesigner.repository;

import com.beforehairshop.demo.hairdesigner.domain.HairDesigner;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerWorkingDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

public interface HairDesignerWorkingDayRepository extends JpaRepository<HairDesignerWorkingDay, BigInteger> {
    List<HairDesignerWorkingDay> findAllByHairDesigner(HairDesigner hairDesigner);
}
