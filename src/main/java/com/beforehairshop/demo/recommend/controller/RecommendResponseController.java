package com.beforehairshop.demo.recommend.controller;

import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.aws.service.AmazonS3Service;
import com.beforehairshop.demo.recommend.service.RecommendService;
import com.beforehairshop.demo.response.ResultDto;
import com.google.firebase.messaging.FirebaseMessagingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigInteger;

@RestController
@Tag(name = "스타일 추천서 수락/거절 관련 Controller")
@AllArgsConstructor
@RequestMapping("/api/v1/recommend/response")
public class RecommendResponseController {

    private final RecommendService recommendService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "스타일 추천서 수락 API")
    @PatchMapping("accept")
    public ResponseEntity<ResultDto> acceptRecommend(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "recommend_id") BigInteger recommendId) throws FirebaseMessagingException, IOException {
        return recommendService.acceptRecommend(principalDetails.getMember(), recommendId);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "스타일 추천서 거절 API")
    @PatchMapping("reject")
    public ResponseEntity<ResultDto> rejectRecommend(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "recommend_id") BigInteger recommendId) {
        return recommendService.rejectRecommend(principalDetails.getMember(), recommendId);
    }
}
