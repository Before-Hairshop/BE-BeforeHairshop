package com.beforehairshop.demo.member.controller;

import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.aws.S3Uploader;
import com.beforehairshop.demo.member.dto.MemberProfilePatchRequestDto;
import com.beforehairshop.demo.member.dto.MemberProfileSaveRequestDto;
import com.beforehairshop.demo.member.service.MemberService;
import com.beforehairshop.demo.response.ResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.beforehairshop.demo.response.ResultDto.*;

@RestController
@Tag(name = "모든 유저 관련 Controller")
@AllArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_DESIGNER')")
    @GetMapping("session")
    @Operation(summary = "본인 정보 조회 API (세션)")
    public ResponseEntity<ResultDto> findMeBySession(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return makeResult(HttpStatus.OK, principalDetails.getMember());
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_DESIGNER')")
    @GetMapping("")
    @Operation(summary = "본인 정보 조회 API (DB)")
    public ResponseEntity<ResultDto> findMe(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return memberService.findMe(principalDetails.getMember());
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_DESIGNER')")
    @PatchMapping("validation")
    @Operation(summary = "약관 동의 + 유저 타입 결정 API (해당 과정 거치면 유효한 유저가 된다) - 디자이너 플래그 : 1이면, 헤어 디자이너 & 0이면, 일반 유저")
    public ResponseEntity<ResultDto> verifyNormal(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "hairDesignerFlag") Integer hairDesignerFlag) {
        return memberService.validation(principalDetails.getMember(), hairDesignerFlag);
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @PatchMapping("/change_to_designer")
    @Operation(summary = "유저에서 디자이너로 권한 변경 API")
    public ResponseEntity<ResultDto> changeToDesigner(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return memberService.changeToDesigner(principalDetails.getMember());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DESIGNER')")
    @PatchMapping("/change_to_user")
    @Operation(summary = "디자이너에서 유저로 권한 변경 API")
    public ResponseEntity<ResultDto> changeToUser(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return memberService.changeToUser(principalDetails.getMember());
    }



}
