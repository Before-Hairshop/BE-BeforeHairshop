package com.beforehairshop.demo.member.controller;

import com.beforehairshop.demo.response.ResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "유저 관련 Controller")
@AllArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {



//    @PostMapping("signup-google")
//    @Operation(summary = "구글 이메일로 가입")
//    public ResponseEntity<ResultDto> signUpGoogle (@RequestBody MemberSaveDto memberSaveDto) {
//        return memberService.signUpGoogle(memberSaveDto);
//    }

}
