package com.beforehairshop.demo.recommend.controller;

import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.aws.service.AmazonS3Service;
import com.beforehairshop.demo.recommend.dto.patch.RecommendPatchRequestDto;
import com.beforehairshop.demo.recommend.dto.post.RecommendSaveRequestDto;
import com.beforehairshop.demo.recommend.service.RecommendService;
import com.beforehairshop.demo.response.ResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    @PreAuthorize("hasAnyRole('USER', 'DESIGNER', 'ADMIN')")
    @Operation(summary = "스타일 추천서 한 개 조회")
    @GetMapping("")
    public ResponseEntity<ResultDto> findOne(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "recommend_id") BigInteger recommendId) {
        return recommendService.findOne(principalDetails.getMember(), recommendId);
    }

//    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
//    @Operation(summary = "(위치 순서) 추천받은 스타일 추천서 조회 API")
//    @GetMapping("list_by_location")
//    public ResponseEntity<ResultDto> findMany(@AuthenticationPrincipal PrincipalDetails principalDetails
//            , @PageableDefault(size = 5) Pageable pageable) {
//        return recommendService.findManyByMe(principalDetails.getMember(), pageable);
//    }

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
            , @RequestParam(name = "recommend_id") BigInteger recommendId
            , @RequestParam(name = "image_count") Integer imageCount) {
        return recommendService.saveImage(principalDetails.getMember(), recommendId, imageCount, amazonS3Service);
    }

    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "스타일 추천서 수정 (이미지 제외)")
    @PatchMapping("")
    public ResponseEntity<ResultDto> patch(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "recommend_id") BigInteger recommendId
            , @RequestBody RecommendPatchRequestDto recommendPatchRequestDto) {
        return recommendService.patch(principalDetails.getMember(), recommendId, recommendPatchRequestDto);
    }

    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "스타일 추천서 수정 (이미지 제외)")
    @PatchMapping("image")
    public ResponseEntity<ResultDto> patchImage(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "recommend_id") BigInteger styleRecommendId
            , @RequestParam(name = "add_image_count") Integer addImageCount
            , String[] deleteImageUrl) {
        return recommendService.patchImage(principalDetails.getMember(), styleRecommendId, addImageCount, deleteImageUrl, amazonS3Service);
    }


}
