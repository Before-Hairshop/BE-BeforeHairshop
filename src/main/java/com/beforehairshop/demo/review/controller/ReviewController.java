package com.beforehairshop.demo.review.controller;

import com.beforehairshop.demo.response.ResultDto;
import com.beforehairshop.demo.review.domain.Review;
import com.beforehairshop.demo.review.dto.ReviewSaveRequestDto;
import com.beforehairshop.demo.review.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "리뷰 관련 Controller")
@AllArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping()
    public ResponseEntity<ResultDto> save(@RequestBody ReviewSaveRequestDto reviewSaveRequestDto) {
        return reviewService.save(reviewSaveRequestDto);
    }
}
