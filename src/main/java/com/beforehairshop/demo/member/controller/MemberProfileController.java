package com.beforehairshop.demo.member.controller;

import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.aws.S3Uploader;
import com.beforehairshop.demo.aws.service.AmazonS3Service;
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

@RestController
@Tag(name = "일반 유저 프로필 관련 Controller")
@AllArgsConstructor
@RequestMapping("/api/v1/members/profiles")
public class MemberProfileController {
    private final MemberService memberService;
    private final S3Uploader s3Uploader;
    private final AmazonS3Service amazonS3Service;


    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "유저-본인 프로필 조회 API")
    @GetMapping("")
    public ResponseEntity<ResultDto> findMyProfile(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return memberService.findMyProfile(principalDetails.getMember());
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "유저-본인 프로필 저장 API(이미지 제외)")
    @PostMapping("")
    public ResponseEntity<ResultDto> saveMemberProfile(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestBody MemberProfileSaveRequestDto memberProfileSaveRequestDto) {

        return memberService.saveMemberProfile(principalDetails.getMember(), memberProfileSaveRequestDto);
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "유저-본인 프로필 저장 API(이미지)")
    @PostMapping("image")
    public ResponseEntity<ResultDto> saveMemberProfileImage(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "front_image_flag") Integer frontImageFlag
            , @RequestParam(name = "side_image_flag") Integer sideImageFlag
            , @RequestParam(name = "back_image_flag") Integer backImageFlag) {

        return memberService.saveMemberProfileImage(principalDetails.getMember(), frontImageFlag, sideImageFlag, backImageFlag, amazonS3Service);
    }


    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "유저-본인 프로필 수정 API(이미지 제외)")
    @PatchMapping("")
    public ResponseEntity<ResultDto> patchMyProfile(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestBody MemberProfilePatchRequestDto memberProfilePatchRequestDto) {
        return memberService.patchMyProfile(principalDetails.getMember(), memberProfilePatchRequestDto);
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "유저-본인 프로필 수정 API(이미지)")
    @PatchMapping("/image")
    public ResponseEntity<ResultDto> patchMyProfileImage(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "front_image_flag") Integer frontImageFlag
            , @RequestParam(name = "side_image_flag") Integer sideImageFlag
            , @RequestParam(name = "back_image_flag") Integer backImageFlag) {
        return memberService.patchMyProfileImage(principalDetails.getMember(), frontImageFlag, sideImageFlag, backImageFlag, amazonS3Service);
    }
}
