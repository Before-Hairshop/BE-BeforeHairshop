package com.beforehairshop.demo.ai.controller;

import com.beforehairshop.demo.ai.service.AIService;
import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.response.ResultDto;
import io.swagger.v3.oas.annotations.Operation;
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


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추론 성공"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_DESIGNER', 'ROLE_ADMIN')")
    @Operation(summary = "유저 이미지 등록 (S3의 PreSigned_url 발급)")
    @PostMapping("member_image")
    public ResponseEntity<ResultDto> saveMemberImage(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return aiService.saveMemberImage(principalDetails.getMember());
    }


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추론 성공"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_DESIGNER', 'ROLE_ADMIN')")
    @Operation(summary = "(일반) AI를 이용한 가상 헤어스타일링 결과 추론")
    @PostMapping("")
    public ResponseEntity<ResultDto> inference(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "member_image_id")BigInteger memberImageId) {
        return aiService.inference(principalDetails.getMember(), memberImageId);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추론 성공"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_DESIGNER', 'ROLE_ADMIN')")
    @Operation(summary = "(프리미엄) AI를 이용한 가상 헤어스타일링 결과 추론")
    @PostMapping("premium")
    public ResponseEntity<ResultDto> premiumInference(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "member_image_id")BigInteger memberImageId) {
        return aiService.premiumInference(principalDetails.getMember(), memberImageId);
    }


    @PostMapping("test")
    public ResponseEntity<ResultDto> sqsTest(@RequestParam(name = "member_id") BigInteger memberId
            , @RequestParam(name = "member_image_id") BigInteger memberImageId) {
        return aiService.testSqs(memberId, memberImageId);
    }

}
