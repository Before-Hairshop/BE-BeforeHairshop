package com.beforehairshop.demo.member.controller;

import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.member.dto.MemberDto;
import com.beforehairshop.demo.member.service.MemberService;
import com.beforehairshop.demo.response.ResultDto;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@Tag(name = "모든 유저 관련 Controller")
@AllArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유저 조회 성공 (세션)", content = @Content(schema = @Schema(implementation = MemberDto.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("session")
    @Operation(summary = "본인 정보 조회 API (세션)")
    public ResponseEntity<ResultDto> findMeBySession(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return memberService.findMeBySession(principalDetails.getMember());
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유저 조회 성공 (DB)", content = @Content(schema = @Schema(implementation = MemberDto.class))),
            @ApiResponse(responseCode = "400", description = "DB에 저장되어 있는 유저가 아니다", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_DESIGNER')")
    @GetMapping("")
    @Operation(summary = "본인 정보 조회 API (DB)")
    public ResponseEntity<ResultDto> findMe(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return memberService.findMeByDB(principalDetails.getMember());
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "약관 동의 + 유저 타입 결정 성공", content = @Content(schema = @Schema(implementation = MemberDto.class))),
            @ApiResponse(responseCode = "400", description = "이미 약관 동의 + 유저 타입을 결정한 유저이다", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_DESIGNER')")
    @PatchMapping("validation")
    @Operation(summary = "약관 동의 + 유저 타입 결정 API (해당 과정 거치면 유효한 유저가 된다) - 디자이너 플래그 : 1이면, 헤어 디자이너 & 0이면, 일반 유저")
    public ResponseEntity<ResultDto> verifyNormal(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "hair_designer_flag") Integer hairDesignerFlag) {
        return memberService.validation(principalDetails.getMember(), hairDesignerFlag);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "권한 변경 성공", content = @Content(schema = @Schema(implementation = MemberDto.class))),
            @ApiResponse(responseCode = "400", description = "저장된 유저가 아니다", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @PatchMapping("/change_to_designer")
    @Operation(summary = "유저에서 디자이너로 권한 변경 API")
    public ResponseEntity<ResultDto> changeToDesigner(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return memberService.changeToDesigner(principalDetails.getMember());
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "권한 변경 성공", content = @Content(schema = @Schema(implementation = MemberDto.class))),
            @ApiResponse(responseCode = "400", description = "저장된 유저가 아니다", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DESIGNER')")
    @PatchMapping("/change_to_user")
    @Operation(summary = "디자이너에서 유저로 권한 변경 API")
    public ResponseEntity<ResultDto> changeToUser(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return memberService.changeToUser(principalDetails.getMember());
    }

//    @PostMapping("/sign_in/kakao")
//    @Operation(summary = "회원가입(카카오)")
//    public ResponseEntity<ResultDto> signInKako(@RequestBody "member save dto") {
//
//    }



}
