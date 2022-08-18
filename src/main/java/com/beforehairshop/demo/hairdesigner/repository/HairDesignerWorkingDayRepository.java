package com.beforehairshop.demo.hairdesigner.repository;

import com.beforehairshop.demo.hairdesigner.domain.HairDesigner;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerWorkingDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HairDesignerWorkingDayRepository extends JpaRepository<HairDesignerWorkingDay, Long> {
    List<HairDesignerWorkingDay> findAllByHairDesigner(HairDesigner hairDesigner);
}
