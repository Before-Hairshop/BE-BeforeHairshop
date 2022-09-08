package com.beforehairshop.demo.member.controller;

import com.beforehairshop.demo.auth.PrincipalDetails;
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

import static com.beforehairshop.demo.response.ResultDto.*;

@RestController
@Tag(name = "유저 관련 Controller")
@AllArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping("test")
    public ResponseEntity<ResultDto> testFind(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return makeResult(HttpStatus.OK, principalDetails.getMember());
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("")
    public ResponseEntity<ResultDto> findMyProfile(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return memberService.findMyProfile(principalDetails.getMember());
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("")
    public ResponseEntity<ResultDto> saveMemberProfile(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestBody MemberProfileSaveRequestDto memberProfileSaveRequestDto) {
        return memberService.saveMemberProfile(principalDetails.getMember(), memberProfileSaveRequestDto);
    }

//    @PostMapping("signup-google")
//    @Operation(summary = "구글 이메일로 가입")
//    public ResponseEntity<ResultDto> signUpGoogle (@RequestBody MemberSaveDto memberSaveDto) {
//        return memberService.signUpGoogle(memberSaveDto);
//    }

}
