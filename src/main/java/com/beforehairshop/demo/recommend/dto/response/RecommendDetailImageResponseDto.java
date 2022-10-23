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
public class RecommendDetailImageResponseDto {
    private BigInteger designerId;
    private String designerName;
    private String designerImage;
    private String userPhoneNumber;

    private RecommendDto recommendDto;
    private List<RecommendImageDto> recommendImageDtoList;
}
