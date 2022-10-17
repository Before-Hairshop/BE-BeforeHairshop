package com.beforehairshop.demo.review.controller;

import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.aws.service.AmazonS3Service;
import com.beforehairshop.demo.member.dto.MemberDto;
import com.beforehairshop.demo.response.ResultDto;
import com.beforehairshop.demo.review.dto.ReviewDto;
import com.beforehairshop.demo.review.dto.patch.ReviewPatchRequestDto;
import com.beforehairshop.demo.review.dto.response.ReviewDetailResponseDto;
import com.beforehairshop.demo.review.dto.save.ReviewSaveRequestDto;
import com.beforehairshop.demo.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

@RestController
@Tag(name = "리뷰 관련 Controller")
@AllArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final AmazonS3Service amazonS3Service;


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공 (없으면 Null)"
                    , content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReviewDetailResponseDto.class)))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('USER', 'DESIGNER', 'ADMIN')")
    @Operation(summary = "리뷰 목록 조회")
    @GetMapping("list")
    public ResponseEntity<ResultDto> findMany(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "hair_designer_id") BigInteger hairDesignerId
            , @PageableDefault(size = 5) Pageable pageable) {
        return reviewService.findManyByHairDesigner(principalDetails.getMember(), hairDesignerId, pageable);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리뷰 수정 성공"
                    , content = @Content(schema = @Schema(implementation = ReviewDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 리뷰 ID 입니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "503", description = "해당 리뷰를 수정할 권한이 없습니다"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('USER', 'DESIGNER', 'ADMIN')")
    @Operation(summary = "리뷰 수정(이미지 제외)")
    @PatchMapping("")
    public ResponseEntity<ResultDto> patchOne(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam("review_id") BigInteger reviewId
            , @RequestBody ReviewPatchRequestDto reviewPatchRequestDto) {
        return reviewService.patchOne(principalDetails.getMember(), reviewId, reviewPatchRequestDto);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리뷰 이미지 수정 성공"
                    , content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
            @ApiResponse(responseCode = "400", description = "Request body 에 잘못된 이미지 url 가 입력됐다"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "잘못된 리뷰 ID 입니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "503", description = "해당 리뷰를 수정할 권한이 없습니다"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('USER', 'DESIGNER', 'ADMIN')")
    @Operation(summary = "리뷰 수정(이미지)")
    @PatchMapping("image")
    public ResponseEntity<ResultDto> patchImage(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam("review_id") BigInteger reviewId
            , @RequestParam("add_review_image_count") Integer reviewImageCount
            , String[] deleteImageUrlList) {
        return reviewService.patchImage(principalDetails.getMember(), reviewId, deleteImageUrlList, reviewImageCount, amazonS3Service);
    }


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리뷰 저장 성공"
                    , content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReviewDto.class)))),
            @ApiResponse(responseCode = "204", description = "리뷰 저장에 필요한 정보가 부족함."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "리뷰 대상이 유효하지 않거나, 헤어 디자이너가 아니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('USER', 'DESIGNER', 'ADMIN')")
    @Operation(summary = "리뷰 생성(이미지 제외)")
    @PostMapping()
    public ResponseEntity<ResultDto> save(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestBody ReviewSaveRequestDto reviewSaveRequestDto) {
        return reviewService.save(principalDetails.getMember(), reviewSaveRequestDto);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리뷰 이미지 저장 성공"
                    , content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
            @ApiResponse(responseCode = "404", description = "잘못된 리뷰 ID 입니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "503", description = "해당 리뷰를 수정할 권한이 없습니다"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('USER', 'DESIGNER', 'ADMIN')")
    @Operation(summary = "리뷰 생성(이미지)")
    @PostMapping("image")
    public ResponseEntity<ResultDto> saveImage(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "review_id") BigInteger reviewId
            , @RequestParam(name = "review_image_count") Integer reviewImageCount) {

        return reviewService.saveImage(principalDetails.getMember(), reviewId, reviewImageCount, amazonS3Service);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리뷰 삭제 성공"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "잘못된 리뷰 ID 입니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "503", description = "해당 리뷰를 삭제할 권한이 없습니다"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('USER', 'DESIGNER', 'ADMIN')")
    @Operation(summary = "리뷰 삭제")
    @DeleteMapping("")
    public ResponseEntity<ResultDto> delete(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "review_id") BigInteger reviewId) {
        return reviewService.delete(principalDetails.getMember(), reviewId);
    }

}
