package com.beforehairshop.demo.hairdesigner.repository;

import com.beforehairshop.demo.hairdesigner.domain.HairDesignerHashtag;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface HairDesignerHashtagRepository extends JpaRepository<HairDesignerHashtag, BigInteger> {
    List<HairDesignerHashtag> findAllByHairDesignerProfileAndStatus(HairDesignerProfile hairDesignerProfile, Integer status);
}
