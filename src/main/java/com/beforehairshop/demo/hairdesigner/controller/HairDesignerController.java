package com.beforehairshop.demo.hairdesigner.controller;

import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.hairdesigner.dto.patch.HairDesignerProfilePatchRequestDto;
import com.beforehairshop.demo.hairdesigner.dto.post.HairDesignerProfileSaveRequestDto;
import com.beforehairshop.demo.hairdesigner.service.HairDesignerService;
import com.beforehairshop.demo.response.ResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@Tag(name = "헤어 디자이너 관련 Controller")
@AllArgsConstructor
@RequestMapping("/api/v1/hair_designers")
public class HairDesignerController {

    private final HairDesignerService hairDesignerService;


    @PreAuthorize("hasAnyRole('USER', 'DESIGNER', 'ADMIN')")
    @Operation(summary = "헤어 디자이너 상세 조회")
    @GetMapping()
    public ResponseEntity<ResultDto> findOne(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(value = "hairDesignerId", required = true) BigInteger hairDesignerId) {
        return hairDesignerService.findOne(principalDetails.getMember(), hairDesignerId);
    }

    @PreAuthorize("hasAnyRole('USER', 'DESIGNER', 'ADMIN')")
    @Operation(summary = "위치 기반(1.5km 반경) - 헤어 디자이너 목록 조회")
    @GetMapping("list-by-location")
    public ResponseEntity<ResultDto> findMany(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(value = "pageNumber", required = true) Integer pageNumber) {
        return hairDesignerService.findManyByLocation(principalDetails.getMember(), pageNumber);
    }

    /**
     * 별점 기반, 리뷰 기반으로 헤어 디자이너 조회하는 API 추가해야 함.
     */

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "헤어 디자이너 프로필 생성(이미지 제외)")
    @PostMapping()
    public ResponseEntity<ResultDto> save(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestBody HairDesignerProfileSaveRequestDto hairDesignerProfileSaveRequestDto, Authentication authentication) {

        return hairDesignerService.save(principalDetails.getMember(), hairDesignerProfileSaveRequestDto);
    }

    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "헤어 디자이너 프로필 - 이미지 생성 및 수정(용도 2가지)")
    @PostMapping("image")
    public ResponseEntity<ResultDto> saveImage(@AuthenticationPrincipal PrincipalDetails principalDetails
            , MultipartFile image) throws IOException {
        return hairDesignerService.saveImage(principalDetails.getMember(), image);
    }


    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "헤어 디자이너 프로필 수정(이미지 제외)")
    @PatchMapping()
    public ResponseEntity<ResultDto> patch(@AuthenticationPrincipal PrincipalDetails principalDetail
            , @RequestBody HairDesignerProfilePatchRequestDto hairDesignerProfilePatchRequestDto) {
        return hairDesignerService.patchOne(principalDetail.getMember(), hairDesignerProfilePatchRequestDto);
    }

    /**
     * 별점 기반 or 리뷰 기반으로 헤어 디자이너 조회하는 API 추가해야 함.
     */

}
