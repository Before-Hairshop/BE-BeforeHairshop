package com.beforehairshop.demo.review.controller;

import com.beforehairshop.demo.response.ResultDto;
import com.beforehairshop.demo.review.domain.Review;
import com.beforehairshop.demo.review.dto.ReviewPatchRequestDto;
import com.beforehairshop.demo.review.dto.ReviewSaveRequestDto;
import com.beforehairshop.demo.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@Tag(name = "리뷰 관련 Controller")
@AllArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("list")
    @Operation(summary = "리뷰 목록 조회 API")
    public ResponseEntity<ResultDto> findMany(@RequestParam(name = "hair_designer_id") BigInteger hairDesignerId, @PageableDefault(size = 5) Pageable pageable) {
        return reviewService.findManyByHairDesigner(hairDesignerId, pageable);
    }

    @PatchMapping("{review_id}")
    @Operation(summary = "리뷰 수정 API")
    public ResponseEntity<ResultDto> patchOne(@PathVariable(name = "review_id") BigInteger reviewId, @RequestBody ReviewPatchRequestDto reviewPatchRequestDto) {
        return reviewService.patchOne(reviewId, reviewPatchRequestDto);
    }

    @PostMapping()
    public ResponseEntity<ResultDto> save(@RequestBody ReviewSaveRequestDto reviewSaveRequestDto) {
        return reviewService.save(reviewSaveRequestDto);
    }
}
