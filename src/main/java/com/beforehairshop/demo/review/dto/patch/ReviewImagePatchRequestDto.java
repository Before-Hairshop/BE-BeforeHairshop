package com.beforehairshop.demo.review.dto.patch;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigInteger;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewImagePatchRequestDto {
    private BigInteger reviewId;
    private Integer addReviewImageCount;
    private List<String> deleteImageUrlList;
}
