package com.beforehairshop.demo.ai.controller;

import com.beforehairshop.demo.ai.dto.VirtualMemberImageResponseDto;
import com.beforehairshop.demo.ai.service.AIService;
import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.aws.service.AmazonS3Service;
import com.beforehairshop.demo.response.ResultDto;
import com.beforehairshop.demo.review.dto.response.ReviewDetailResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@Tag(name = "AI 이용한 가상헤어스타일링 관련 컨트롤러")
@AllArgsConstructor
@RequestMapping("/api/v1/virtual_hairstyling")
public class AIController {
    private final AIService aiService;
    private final AmazonS3Service amazonS3Service;


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이미지 목록 조회 성공"
                    , content = @Content(array = @ArraySchema(schema = @Schema(implementation = VirtualMemberImageResponseDto.class)))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_DESIGNER', 'ROLE_ADMIN')")
    @Operation(summary = "가상 헤어스타일링용 유저 이미지 목록 조회")
    @GetMapping("")
    public ResponseEntity<ResultDto> getMyVirtualMemberImageList(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return aiService.getMyVirtualMemberImageList(principalDetails.getMember());
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "inference 된 result 이미지 목록 조회 성공"
                    , content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
            @ApiResponse(responseCode = "400", description = "잘못된 유저 이미지 ID 입니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "501", description = "inference 되지 않은 유저 이미지입니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "503", description = "조회할 권한이 없는 유저입니다"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_DESIGNER', 'ROLE_ADMIN')")
    @Operation(summary = "가상 헤어스타일링 결과 이미지 목록 조회")
    @GetMapping("inference_result")
    public ResponseEntity<ResultDto> getInferenceResultList(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "virtual_member_image_id") BigInteger virtualMemberImageId) {
        return aiService.getInferenceResultList(principalDetails.getMember(), virtualMemberImageId);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "(pre) inference 된 result 이미지 목록 조회 성공"
                    , content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
            @ApiResponse(responseCode = "400", description = "잘못된 유저 이미지 ID 입니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_DESIGNER', 'ROLE_ADMIN')")
    @Operation(summary = "[PRE] 이미 inference 된 가상 헤어스타일링 결과 이미지 목록 조회 (남자 : 1, 여자 : 2)")
    @GetMapping("inference_result_pre")
    public ResponseEntity<ResultDto> getPreInferenceResultList(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "pre_input_image_id") Integer preInputImageId) {
        return aiService.getPreInferenceResultList(principalDetails.getMember(), preInputImageId);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이미지 저장 성공 (presigned url 발급)"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_DESIGNER', 'ROLE_ADMIN')")
    @Operation(summary = "가상 헤어스타일링용 유저 이미지 등록 (S3의 PreSigned_url 발급)")
    @PostMapping("")
    public ResponseEntity<ResultDto> saveVirtualMemberImage(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return aiService.saveVirtualMemberImage(principalDetails.getMember(), amazonS3Service);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이미지 삭제 성공"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_DESIGNER', 'ROLE_ADMIN')")
    @Operation(summary = "가상 헤어스타일링용 유저 이미지 삭제")
    @DeleteMapping("")
    public ResponseEntity<ResultDto> deleteVirtualMemberImage(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "virtual_member_image_url") String virtualMemberImageUrl) {
        return aiService.deleteVirtualMemberImage(principalDetails.getMember(), virtualMemberImageUrl);
    }


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추론 성공"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_DESIGNER', 'ROLE_ADMIN')")
    @Operation(summary = "(일반) AI를 이용한 가상 헤어스타일링 결과 추론")
    @PostMapping("inference")
    public ResponseEntity<ResultDto> inference(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "virtual_member_image_id")BigInteger virtualMemberImageId) {
        return aiService.inference(principalDetails.getMember(), virtualMemberImageId);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추론 성공"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @Operation(summary = "(테스트) AI를 이용한 가상 헤어스타일링 결과 추론")
    @PostMapping("inference_test")
    public ResponseEntity<ResultDto> inferenceTest(@RequestParam(name = "memberId")BigInteger memberId, @RequestParam(name = "virtual_member_image_id")BigInteger virtualMemberImageId) {
        return aiService.inferenceTest(memberId, virtualMemberImageId);
    }

//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "추론 성공"
//                    , content = @Content(schema = @Schema(implementation = String.class))),
//            @ApiResponse(responseCode = "504", description = "세션 만료"
//                    , content = @Content(schema = @Schema(implementation = String.class)))
//    })
//    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_DESIGNER', 'ROLE_ADMIN')")
//    @Operation(summary = "(프리미엄) AI를 이용한 가상 헤어스타일링 결과 추론")
//    @PostMapping("inference/premium")
//    public ResponseEntity<ResultDto> premiumInference(@AuthenticationPrincipal PrincipalDetails principalDetails
//            , @RequestParam(name = "virtual_member_image_id")BigInteger virtualMemberImageId) {
//        return aiService.premiumInference(principalDetails.getMember(), virtualMemberImageId);
//    }


    @PostMapping("test")
    public ResponseEntity<ResultDto> sqsTest(@RequestParam(name = "member_id") BigInteger memberId
            , @RequestParam(name = "virtual_member_image_id") BigInteger virtualMemberImageId) {
        return aiService.testSqs(memberId, virtualMemberImageId);
    }

}
