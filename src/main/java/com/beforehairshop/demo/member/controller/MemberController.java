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

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping("")
    @Operation(summary = "본인 정보 조회 API")
    public ResponseEntity<ResultDto> findMe(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return makeResult(HttpStatus.OK, principalDetails.getMember());
    }

}
