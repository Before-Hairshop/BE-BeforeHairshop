package com.beforehairshop.demo.hairdesigner.controller;

import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.aws.service.AmazonS3Service;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerProfileDto;
import com.beforehairshop.demo.hairdesigner.dto.patch.HairDesignerProfilePatchRequestDto;
import com.beforehairshop.demo.hairdesigner.dto.post.HairDesignerProfileSaveRequestDto;
import com.beforehairshop.demo.hairdesigner.dto.response.HairDesignerDetailGetResponseDto;
import com.beforehairshop.demo.hairdesigner.dto.response.HairDesignerProfileAndHashtagDto;
import com.beforehairshop.demo.hairdesigner.dto.response.HairDesignerProfileImageResponseDto;
import com.beforehairshop.demo.hairdesigner.service.HairDesignerService;
import com.beforehairshop.demo.member.dto.MemberDto;
import com.beforehairshop.demo.member.dto.MemberProfileDto;
import com.beforehairshop.demo.response.ResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "본인 프로필(디자이너) 조회 성공"
                    , content = @Content(schema = @Schema(implementation = HairDesignerProfileDto.class))),
            @ApiResponse(responseCode = "400", description = "해당 유저는 디자이너가 아님."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "아직 프로필을 등록하지 않은 유저이다"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "(본인)헤어 디자이너 프로필 조회 API - 메인 페이지")
    @GetMapping("")
    public ResponseEntity<ResultDto> findMe(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return hairDesignerService.findMe(principalDetails.getMember());
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 상세 조회 성공 (상세 페이지)"
                    , content = @Content(schema = @Schema(implementation = HairDesignerDetailGetResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "해당 유저는 등록되어 있지 않거나, 디자이너가 아님."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "해당 디자이너는 프로필이 없다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('USER', 'DESIGNER', 'ADMIN')")
    @Operation(summary = "헤어 디자이너 상세 조회(id) - 상세 조회 페이지")
    @GetMapping("id")
    public ResponseEntity<ResultDto> findOne(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(value = "hair_designer_id", required = true) BigInteger hairDesignerId) {
        return hairDesignerService.findOne(principalDetails.getMember(), hairDesignerId);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이름으로 디자이너 프로필 조회 (프로필 없어도, 조회 성공(null 반환))"
                    , content = @Content(array = @ArraySchema(schema = @Schema(implementation = HairDesignerProfileAndHashtagDto.class)))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('USER', 'DESIGNER', 'ADMIN')")
    @Operation(summary = "(이름) 헤어 디자이너 List 조회 API (5명씩)")
    @GetMapping("list_by_name")
    public ResponseEntity<ResultDto> findAllByName(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(value = "name", required = true) String name
            , @PageableDefault(size = 5) Pageable pageable) {
        return hairDesignerService.findAllByName(principalDetails.getMember(), name, pageable);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "위치 기반으로 디자이너 프로필 조회 (프로필 없어도, 조회 성공(null 반환))"
                    , content = @Content(array = @ArraySchema(schema = @Schema(implementation = HairDesignerProfileAndHashtagDto.class)))),
            @ApiResponse(responseCode = "400", description = "조회하려는 유저의 프로필이 등록되어 있지 않습니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('USER', 'DESIGNER', 'ADMIN')")
    @Operation(summary = "(위치 순서 : 10km 안) - 헤어 디자이너 목록 조회")
    @GetMapping("list_by_location")
    public ResponseEntity<ResultDto> findMany(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(value = "page_number", required = true) Integer pageNumber) {
        return hairDesignerService.findManyByLocation(principalDetails.getMember(), pageNumber);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "디자이너 프로필 저장 성공"
                    , content = @Content(schema = @Schema(implementation = HairDesignerProfileDto.class))),
            @ApiResponse(responseCode = "400", description = "조회하려는 유저는 일반 유저입니다. 디자이너로 권한 변경을 하고 등록해주시기 바랍니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "이미 프로필이 존재합니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "삭제되었거나 유효하지 않은 유저입니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "헤어 디자이너 프로필 생성(이미지 제외)")
    @PostMapping()
    public ResponseEntity<ResultDto> save(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestBody HairDesignerProfileSaveRequestDto hairDesignerProfileSaveRequestDto) {

        return hairDesignerService.save(principalDetails.getMember(), hairDesignerProfileSaveRequestDto);
    }


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "디자이너 프로필 이미지 저장 성공"
                    , content = @Content(schema = @Schema(implementation = HairDesignerProfileImageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "이 유저는 헤어 디자이너가 아닙니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "해당 디자이너의 프로필이 존재하지 않습니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "헤어 디자이너 프로필 생성(이미지)")
    @PostMapping("image")
    public ResponseEntity<ResultDto> saveImage(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return hairDesignerService.saveImage(principalDetails.getMember(), amazonS3Service);
    }


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "디자이너 프로필 수정 성공"
                    , content = @Content(schema = @Schema(implementation = HairDesignerProfileDto.class))),
            @ApiResponse(responseCode = "400", description = "이 유저는 유효하지 않거나, 헤어 디자이너가 아닙니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "해당 디자이너의 프로필이 존재하지 않습니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "헤어 디자이너 프로필 수정(이미지 제외)")
    @PatchMapping()
    public ResponseEntity<ResultDto> patch(@AuthenticationPrincipal PrincipalDetails principalDetail
            , @RequestBody HairDesignerProfilePatchRequestDto hairDesignerProfilePatchRequestDto) {
        return hairDesignerService.patchOne(principalDetail.getMember(), hairDesignerProfilePatchRequestDto);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "디자이너 프로필 이미지 수정 성공"
                    , content = @Content(schema = @Schema(implementation = HairDesignerProfileImageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "이 유저는 유효하지 않거나, 헤어 디자이너가 아닙니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "해당 디자이너의 프로필이 존재하지 않습니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "헤어 디자이너 프로필 수정(이미지)")
    @PatchMapping("image")
    public ResponseEntity<ResultDto> patchImage(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return hairDesignerService.patchImage(principalDetails.getMember(), amazonS3Service);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "디자이너 프로필 삭제 성공"
                    , content = @Content(schema = @Schema(implementation = MemberDto.class))),
            @ApiResponse(responseCode = "400", description = "이 유저는 유효하지 않거나, 헤어 디자이너가 아닙니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "해당 디자이너의 프로필이 존재하지 않습니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "헤어 디자이너 프로필 제거(권한 변경 X)")
    @DeleteMapping("")
    public ResponseEntity<ResultDto> deleteProfile(@AuthenticationPrincipal PrincipalDetails principalDetail) {
        return hairDesignerService.deleteProfile(principalDetail.getMember());
    }

}
