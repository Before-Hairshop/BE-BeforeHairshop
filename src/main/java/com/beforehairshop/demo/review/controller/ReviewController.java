package com.beforehairshop.demo.review.controller;

import com.beforehairshop.demo.review.domain.Review;
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

//    @PostMapping()
//    public ResponseEntity<Review> save(@RequestBody ReviewSaveRequestDto reviewSaveRequestDto) {
//
//    }
}
