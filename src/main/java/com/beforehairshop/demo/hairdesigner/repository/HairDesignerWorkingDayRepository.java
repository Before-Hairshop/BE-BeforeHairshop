package com.beforehairshop.demo.hairdesigner.repository;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerWorkingDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface HairDesignerWorkingDayRepository extends JpaRepository<HairDesignerWorkingDay, BigInteger> {
    List<HairDesignerWorkingDay> findAllByHairDesignerProfileAndStatus(HairDesignerProfile hairDesignerProfile, Integer status);


}
