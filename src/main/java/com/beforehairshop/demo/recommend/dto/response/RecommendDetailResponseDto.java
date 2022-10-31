package com.beforehairshop.demo.recommend.dto.response;

import com.beforehairshop.demo.recommend.dto.RecommendDto;
import com.beforehairshop.demo.recommend.dto.RecommendImageDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendDetailResponseDto {
    private BigInteger designerId;
    private String designerName;
    private String designerImage;
    private String designerPhoneNumber;

    private String customerName;
    private String customerImage;
    private String customerPhoneNumber;

    private RecommendDto recommendDto;
}
