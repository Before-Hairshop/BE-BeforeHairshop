package com.beforehairshop.demo.hairdesigner.controller;

import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.aws.service.AmazonS3Service;
import com.beforehairshop.demo.hairdesigner.dto.patch.HairDesignerProfilePatchRequestDto;
import com.beforehairshop.demo.hairdesigner.dto.post.HairDesignerProfileSaveRequestDto;
import com.beforehairshop.demo.hairdesigner.service.HairDesignerService;
import com.beforehairshop.demo.response.ResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

@RestController
@Tag(name = "헤어 디자이너 관련 Controller")
@AllArgsConstructor
@RequestMapping("/api/v1/hair_designers")
public class HairDesignerController {

    private final HairDesignerService hairDesignerService;
    private final AmazonS3Service amazonS3Service;


    @PreAuthorize("hasAnyRole('USER', 'DESIGNER', 'ADMIN')")
    @Operation(summary = "헤어 디자이너 상세 조회(id)")
    @GetMapping("id")
    public ResponseEntity<ResultDto> findOne(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(value = "hair_designer_id", required = true) BigInteger hairDesignerId) {
        return hairDesignerService.findOne(principalDetails.getMember(), hairDesignerId);
    }

    @PreAuthorize("hasAnyRole('USER', 'DESIGNER', 'ADMIN')")
    @Operation(summary = "(이름) 헤어 디자이너 List 조회 API (5명씩)")
    @GetMapping("list_by_name")
    public ResponseEntity<ResultDto> findAllByName(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(value = "name", required = true) String name
            , @PageableDefault(size = 5) Pageable pageable) {
        return hairDesignerService.findAllByName(principalDetails.getMember(), name, pageable);
    }

    @PreAuthorize("hasAnyRole('USER', 'DESIGNER', 'ADMIN')")
    @Operation(summary = "(위치 순서 : 10km 안) - 헤어 디자이너 목록 조회")
    @GetMapping("list_by_location")
    public ResponseEntity<ResultDto> findMany(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(value = "page_number", required = true) Integer pageNumber) {
        return hairDesignerService.findManyByLocation(principalDetails.getMember(), pageNumber);
    }

    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "헤어 디자이너 프로필 생성(이미지 제외)")
    @PostMapping()
    public ResponseEntity<ResultDto> save(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestBody HairDesignerProfileSaveRequestDto hairDesignerProfileSaveRequestDto) {

        return hairDesignerService.save(principalDetails.getMember(), hairDesignerProfileSaveRequestDto);
    }

    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "헤어 디자이너 프로필 생성(이미지)")
    @PostMapping("image")
    public ResponseEntity<ResultDto> saveImage(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return hairDesignerService.saveImage(principalDetails.getMember(), amazonS3Service);
    }


    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "헤어 디자이너 프로필 수정(이미지 제외)")
    @PatchMapping()
    public ResponseEntity<ResultDto> patch(@AuthenticationPrincipal PrincipalDetails principalDetail
            , @RequestBody HairDesignerProfilePatchRequestDto hairDesignerProfilePatchRequestDto) {
        return hairDesignerService.patchOne(principalDetail.getMember(), hairDesignerProfilePatchRequestDto);
    }

    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "헤어 디자이너 프로필 수정(이미지)")
    @PatchMapping("image")
    public ResponseEntity<ResultDto> patchImage(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return hairDesignerService.patchImage(principalDetails.getMember(), amazonS3Service);
    }

    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "헤어 디자이너 프로필 제거(권한 변경 X)")
    @DeleteMapping("")
    public ResponseEntity<ResultDto> deleteProfile(@AuthenticationPrincipal PrincipalDetails principalDetail) {
        return hairDesignerService.deleteProfile(principalDetail.getMember());
    }

    /**
     * 별점 기반 or 리뷰 기반으로 헤어 디자이너 조회하는 API 추가해야 함.
     */

}
