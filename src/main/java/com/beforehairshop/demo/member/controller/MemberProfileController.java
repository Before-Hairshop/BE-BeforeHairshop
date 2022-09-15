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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@Tag(name = "일반 유저 프로필 관련 Controller")
@AllArgsConstructor
@RequestMapping("/api/v1/members/profiles")
public class MemberProfileController {
    private final MemberService memberService;
    private final S3Uploader s3Uploader;


    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "유저-본인 프로필 조회 API")
    @GetMapping("")
    public ResponseEntity<ResultDto> findMyProfile(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return memberService.findMyProfile(principalDetails.getMember());
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "유저-본인 프로필 저장 API")
    @PostMapping("")
    public ResponseEntity<ResultDto> saveMemberProfile(@AuthenticationPrincipal PrincipalDetails principalDetails
            , MemberProfileSaveRequestDto memberProfileSaveRequestDto) throws IOException {

        return memberService.saveMemberProfile(principalDetails.getMember(), memberProfileSaveRequestDto, s3Uploader);
    }


    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "유저-본인 프로필 수정 API")
    @PatchMapping("")
    public ResponseEntity<ResultDto> patchMyProfile(@AuthenticationPrincipal PrincipalDetails principalDetails
            , MemberProfilePatchRequestDto memberProfilePatchRequestDto) throws IOException {
        return memberService.patchMyProfile(principalDetails.getMember(), memberProfilePatchRequestDto, s3Uploader);
    }
}
