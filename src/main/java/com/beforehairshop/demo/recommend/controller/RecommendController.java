package com.beforehairshop.demo.recommend.controller;

import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.aws.service.AmazonS3Service;
import com.beforehairshop.demo.recommend.dto.post.RecommendSaveRequestDto;
import com.beforehairshop.demo.recommend.service.RecommendService;
import com.beforehairshop.demo.response.ResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@Tag(name = "스타일 추천서 관련 Controller")
@AllArgsConstructor
@RequestMapping("/api/v1/recommend")
public class RecommendController {

    private final RecommendService recommendService;
    private final AmazonS3Service amazonS3Service;

    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "스타일 추천서 생성 (이미지 제외)")
    @PostMapping("")
    public ResponseEntity<ResultDto> save(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "member_profile_id") BigInteger memberProfileId
            , @RequestBody RecommendSaveRequestDto recommendSaveRequestDto) {
        return recommendService.save(principalDetails.getMember(), memberProfileId, recommendSaveRequestDto);
    }

    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "스타일 추천서 생성 (이미지)")
    @PostMapping("image")
    public ResponseEntity<ResultDto> saveImage(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "style_recommend_id") BigInteger styleRecommendId
            , @RequestParam(name = "image_count") Integer imageCount) {
        return recommendService.saveImage(principalDetails.getMember(), styleRecommendId, imageCount, amazonS3Service);
    }

}