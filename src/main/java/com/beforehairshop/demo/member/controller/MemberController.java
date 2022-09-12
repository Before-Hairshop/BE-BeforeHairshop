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
@Tag(name = "유저 관련 Controller")
@AllArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;
    private final S3Uploader s3Uploader;

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping("")
    public ResponseEntity<ResultDto> findMe(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return makeResult(HttpStatus.OK, principalDetails.getMember());
    }

//    @Tag(name = "유저 이미지 수정 API", description = "유저나 헤어 디자이너 모두 이미지를 바꿀 때 사용하는 API")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @PatchMapping("/image")
    public ResponseEntity<ResultDto> patchMyImage(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestBody MultipartFile myImageFile) throws IOException {

        return makeResult(HttpStatus.OK, s3Uploader.upload(myImageFile, principalDetails.getMember().getId().toString() + "/profile.jpg"));
    }

}
