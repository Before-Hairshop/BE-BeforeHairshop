package com.beforehairshop.demo.recommend.dto.post;

import com.beforehairshop.demo.recommend.domain.RecommendRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendRequestSaveRequestDto {
    private BigInteger hairDesignerProfileId;

}
