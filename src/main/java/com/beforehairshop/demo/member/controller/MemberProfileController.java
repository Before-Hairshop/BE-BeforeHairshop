package com.beforehairshop.demo.member.controller;

import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.aws.service.AmazonS3Service;
import com.beforehairshop.demo.member.dto.MemberDto;
import com.beforehairshop.demo.member.dto.MemberProfileDto;
import com.beforehairshop.demo.member.dto.patch.MemberProfilePatchRequestDto;
import com.beforehairshop.demo.member.dto.post.MemberProfileSaveRequestDto;
import com.beforehairshop.demo.member.dto.response.MemberProfileImageResponseDto;
import com.beforehairshop.demo.member.service.MemberService;
import com.beforehairshop.demo.response.ResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@Tag(name = "일반 고객 프로필 관련 Controller")
@AllArgsConstructor
@RequestMapping("/api/v1/members/profiles")
public class MemberProfileController {
    private final MemberService memberService;
    private final AmazonS3Service amazonS3Service;


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "본인 프로필 조회 성공 (프로필 없어도, 조회 성공(null 반환))", content = @Content(schema = @Schema(implementation = MemberProfileDto.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "유저-본인 프로필 조회 API")
    @GetMapping("")
    public ResponseEntity<ResultDto> findMyProfile(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return memberService.findMyProfile(principalDetails.getMember());
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "위치 기반으로 유저들의 프로필 조회 성공 (프로필 없어도, 조회 성공(null 반환))"
                    , content = @Content(array = @ArraySchema(schema = @Schema(implementation = MemberProfileDto.class)))),
            @ApiResponse(responseCode = "400", description = "요청한 유저가 헤어 디자이너가 아니거나, 삭제된 유저이다"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "헤어 디자이너의 프로필이 없음"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('ROLE_DESIGNER', 'ROLE_ADMIN')")
    @Operation(summary = "(위치 기반 1.5km) 위치 기반 유저들의 프로필 목록 조회 - 헤어 디자이너만 열람 가능")
    @GetMapping("/list_by_location")
    public ResponseEntity<ResultDto> findManyProfileByLocation(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "page_number") Integer pageNumber) {
        return memberService.findManyProfileByLocation(principalDetails.getMember()
                , pageNumber);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유저 프로필 저장 성공"
                    , content = @Content(schema = @Schema(implementation = MemberProfileDto.class))),
            @ApiResponse(responseCode = "204", description = "저장에 필요한 정보를 전부 입력하지 않았음."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "유효하지 않거나, 삭제된 유저이다"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "이미 유저 프로필이 존재함"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "유저-본인 프로필 저장 API(본인 이미지 & 원하는 스타일 이미지 제외)")
    @PostMapping("")
    public ResponseEntity<ResultDto> saveMemberProfile(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestBody MemberProfileSaveRequestDto memberProfileSaveRequestDto) {

        return memberService.saveMemberProfile(principalDetails.getMember(), memberProfileSaveRequestDto);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유저 프로필 이미지 저장 성공"
                    , content = @Content(schema = @Schema(implementation = MemberProfileImageResponseDto.class))),
            @ApiResponse(responseCode = "204", description = "정면 사진은 무조건 입력해야 함.(front_flag 는 1)"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "이미지 저장 전에 프로필 등록이 되어 있지 않다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "유저-본인 프로필 저장 API(본인 이미지 & 원하는 스타일 이미지)")
    @PostMapping("image")
    public ResponseEntity<ResultDto> saveMemberProfileImage(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "front_image_flag") Integer frontImageFlag
            , @RequestParam(name = "side_image_flag") Integer sideImageFlag
            , @RequestParam(name = "back_image_flag") Integer backImageFlag
            , @RequestParam(name = "desired_hairstyle_image_count") Integer desiredHairstyleImageCount) {

        return memberService.saveMemberProfileImage(principalDetails.getMember(), frontImageFlag, sideImageFlag, backImageFlag
                , desiredHairstyleImageCount, amazonS3Service);
    }


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유저 프로필 수정 성공"
                    , content = @Content(schema = @Schema(implementation = MemberProfileDto.class))),
            @ApiResponse(responseCode = "400", description = "삭제됐거나, 유효하지 않은 유저이다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "프로필이 존재하지 않음. 생성부터 이뤄져야 한다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "유저-본인 프로필 수정 API(이미지 제외)")
    @PatchMapping("")
    public ResponseEntity<ResultDto> patchMyProfile(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestBody MemberProfilePatchRequestDto memberProfilePatchRequestDto) {
        return memberService.patchMyProfile(principalDetails.getMember(), memberProfilePatchRequestDto);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유저 프로필 이미지 수정 성공"
                    , content = @Content(schema = @Schema(implementation = MemberProfileImageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "지우고자하는 이미지 url 에 존재하지 않는 이미지 url 을 입력했다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "프로필이 존재하지 않음. 생성부터 이뤄져야 한다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "유저-본인 프로필 수정 API(이미지)")
    @PatchMapping("/image")
    public ResponseEntity<ResultDto> patchMyProfileImage(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "front_image_flag") Integer frontImageFlag
            , @RequestParam(name = "side_image_flag") Integer sideImageFlag
            , @RequestParam(name = "back_image_flag") Integer backImageFlag
            , @RequestParam(name = "add_desired_hairstyle_image_count") Integer addDesiredHairstyleImageCount
            , String[] deleteDesiredImageUrlList) {
        return memberService.patchMyProfileImage(principalDetails.getMember(), frontImageFlag, sideImageFlag, backImageFlag
                , addDesiredHairstyleImageCount, deleteDesiredImageUrlList,  amazonS3Service);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 매칭 활성화 성공"
                    , content = @Content(schema = @Schema(implementation = MemberProfileDto.class))),
            @ApiResponse(responseCode = "400", description = "해당 유저는 프로필이 없음"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "유저 프로필 매칭 활성화 API")
    @PatchMapping("/activate_matching")
    public ResponseEntity<ResultDto> patchMyProfileActivateMatchingFlag(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return memberService.patchMyProfileActivateMatchingFlag(principalDetails.getMember());
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 매칭 비활성화 성공"
                    , content = @Content(schema = @Schema(implementation = MemberProfileDto.class))),
            @ApiResponse(responseCode = "400", description = "해당 유저는 프로필이 없음"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "유저 프로필 매칭 비활성화 API")
    @PatchMapping("/deactivate_matching")
    public ResponseEntity<ResultDto> patchMyProfileDeactivateMatchingFlag(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return memberService.patchMyProfileDeactivateMatchingFlag(principalDetails.getMember());
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 삭제 성공"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "해당 유저는 프로필이 없음"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "유저 프로필 삭제 API")
    @DeleteMapping("")
    public ResponseEntity<ResultDto> deleteMyProfile(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return memberService.deleteMyProfile(principalDetails.getMember());
    }
}
