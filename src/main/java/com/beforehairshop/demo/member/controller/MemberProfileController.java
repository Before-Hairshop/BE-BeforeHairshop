package com.beforehairshop.demo.member.controller;

import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.aws.service.AmazonS3Service;
import com.beforehairshop.demo.member.dto.patch.MemberProfilePatchRequestDto;
import com.beforehairshop.demo.member.dto.post.MemberProfileSaveRequestDto;
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
    private final AmazonS3Service amazonS3Service;


    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "유저-본인 프로필 조회 API")
    @GetMapping("")
    public ResponseEntity<ResultDto> findMyProfile(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return memberService.findMyProfile(principalDetails.getMember());
    }

    @PreAuthorize("hasAnyRole('ROLE_DESIGNER', 'ROLE_ADMIN')")
    @Operation(summary = "(위치 기반 1.5km) 위치 기반 유저들의 프로필 목록 조회 - 헤어 디자이너만 열람 가능")
    @GetMapping("/list_by_location")
    public ResponseEntity<ResultDto> findManyProfileByLocation(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "page_number") Integer pageNumber) {
        return memberService.findManyProfileByLocation(principalDetails.getMember()
                , pageNumber);
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "유저-본인 프로필 저장 API(본인 이미지 & 원하는 스타일 이미지 제외)")
    @PostMapping("")
    public ResponseEntity<ResultDto> saveMemberProfile(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestBody MemberProfileSaveRequestDto memberProfileSaveRequestDto) {

        return memberService.saveMemberProfile(principalDetails.getMember(), memberProfileSaveRequestDto);
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "유저-본인 프로필 저장 API(본인 이미지)")
    @PostMapping("image")
    public ResponseEntity<ResultDto> saveMemberProfileImage(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "front_image_flag") Integer frontImageFlag
            , @RequestParam(name = "side_image_flag") Integer sideImageFlag
            , @RequestParam(name = "back_image_flag") Integer backImageFlag
            , @RequestParam(name = "desired_hairstyle_image_count") Integer desiredHairstyleImageCount) {

        return memberService.saveMemberProfileImage(principalDetails.getMember(), frontImageFlag, sideImageFlag, backImageFlag
                , desiredHairstyleImageCount, amazonS3Service);
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
            , @RequestParam(name = "back_image_flag") Integer backImageFlag
            , @RequestParam(name = "add_desired_hairstyle_image_count") Integer addDesiredHairstyleImageCount
            , String[] deleteImageUrlList) {
        return memberService.patchMyProfileImage(principalDetails.getMember(), frontImageFlag, sideImageFlag, backImageFlag
                , addDesiredHairstyleImageCount, deleteImageUrlList,  amazonS3Service);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "유저 프로필 매칭 활성화 API")
    @PatchMapping("/activate_matching")
    public ResponseEntity<ResultDto> patchMyProfileActivateMatchingFlag(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return memberService.patchMyProfileActivateMatchingFlag(principalDetails.getMember());
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "유저 프로필 매칭 활성화 API")
    @PatchMapping("/deactivate_matching")
    public ResponseEntity<ResultDto> patchMyProfileDeactivateMatchingFlag(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return memberService.patchMyProfileDeactivateMatchingFlag(principalDetails.getMember());
    }
}
