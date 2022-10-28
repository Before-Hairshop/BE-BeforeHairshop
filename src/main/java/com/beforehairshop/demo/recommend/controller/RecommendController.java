package com.beforehairshop.demo.recommend.controller;

import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.aws.service.AmazonS3Service;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerProfileDto;
import com.beforehairshop.demo.member.dto.MemberDto;
import com.beforehairshop.demo.recommend.dto.RecommendDto;
import com.beforehairshop.demo.recommend.dto.patch.RecommendPatchRequestDto;
import com.beforehairshop.demo.recommend.dto.post.RecommendSaveRequestDto;
import com.beforehairshop.demo.recommend.dto.response.RecommendDetailImageResponseDto;
import com.beforehairshop.demo.recommend.dto.response.RecommendDetailResponseDto;
import com.beforehairshop.demo.recommend.service.RecommendService;
import com.beforehairshop.demo.response.ResultDto;
import com.google.firebase.messaging.FirebaseMessagingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigInteger;

@RestController
@Tag(name = "스타일 추천서 관련 Controller")
@AllArgsConstructor
@RequestMapping("/api/v1/recommend")
public class RecommendController {

    private final RecommendService recommendService;
    private final AmazonS3Service amazonS3Service;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천서 조회 성공"
                    , content = @Content(schema = @Schema(implementation = RecommendDetailImageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 추천서 ID 이다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('USER', 'DESIGNER', 'ADMIN')")
    @Operation(summary = "스타일 추천서 한 개 상세 조회")
    @GetMapping("")
    public ResponseEntity<ResultDto> findOne(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "recommend_id") BigInteger recommendId) {
        return recommendService.findOne(principalDetails.getMember(), recommendId);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천받은 추천서들 조회 성공"
                    , content = @Content(array = @ArraySchema(schema = @Schema(implementation = RecommendDetailResponseDto.class)))),
            @ApiResponse(responseCode = "404", description = "해당 유저는 프로필을 등록한 상태가 아닙니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "[일반유저 - 모든 추천서(수락 : 2, 대기 : 1, 거절 : 0)] (위치 순서 : 10km 안) 추천받은 스타일 추천서 조회 API")
    @GetMapping("list_by_user")
    public ResponseEntity<ResultDto> findManyByUser(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return recommendService.findManyByMe(principalDetails.getMember());
    }


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내가 작성한 추천서들 조회 성공"
                    , content = @Content(array = @ArraySchema(schema = @Schema(implementation = RecommendDetailResponseDto.class)))),
            @ApiResponse(responseCode = "400", description = "해당 유저가 디자이너가 아닙니다"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "해당 유저의 디자이너 프로필이 없습니다"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "[디자이너 - 모든 추천서(수락 : 2, 대기 : 1, 거절 : 0)] 내가 작성한 스타일 추천서 조회 API")
    @GetMapping("list_by_designer")
    public ResponseEntity<ResultDto> findManyByDesigner(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return recommendService.findManyByDesigner(principalDetails.getMember());
    }


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천서 저장 성공"
                    , content = @Content(schema = @Schema(implementation = RecommendDto.class))),
            @ApiResponse(responseCode = "400", description = "추천받는 유저의 프로필이 없습니다"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "추천을 보낼 디자이너의 프로필이 없습니다"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "503", description = "추천을 받을 유저가 매칭을 비활성화시켰습니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "스타일 추천서 생성 (이미지 제외)")
    @PostMapping("")
    public ResponseEntity<ResultDto> save(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "member_profile_id") BigInteger memberProfileId
            , @RequestBody RecommendSaveRequestDto recommendSaveRequestDto) throws FirebaseMessagingException, IOException {
        return recommendService.save(principalDetails.getMember(), memberProfileId, recommendSaveRequestDto);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천서 이미지 저장 성공"
                    , content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
            @ApiResponse(responseCode = "400", description = "잘못된 추천서 ID 를 입력받았습니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "스타일 추천서 생성 (이미지)")
    @PostMapping("image")
    public ResponseEntity<ResultDto> saveImage(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "recommend_id") BigInteger recommendId
            , @RequestParam(name = "image_count") Integer imageCount) {
        return recommendService.saveImage(principalDetails.getMember(), recommendId, imageCount, amazonS3Service);
    }


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천서 수정 성공"
                    , content = @Content(schema = @Schema(implementation = RecommendDto.class))),
            @ApiResponse(responseCode = "400", description = "해당 유저는 추천서를 수정할 권한이 없습니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "잘못된 추천서 ID 를 입력받았습니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "스타일 추천서 수정 (이미지 제외)")
    @PatchMapping("")
    public ResponseEntity<ResultDto> patch(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "recommend_id") BigInteger recommendId
            , @RequestBody RecommendPatchRequestDto recommendPatchRequestDto) {
        return recommendService.patch(principalDetails.getMember(), recommendId, recommendPatchRequestDto);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천서 이미지 수정 성공"
                    , content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
            @ApiResponse(responseCode = "400", description = "해당 유저는 추천서를 수정할 권한이 없습니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "잘못된 추천서 ID 를 입력받았습니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "스타일 추천서 수정 (이미지)")
    @PatchMapping("image")
    public ResponseEntity<ResultDto> patchImage(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "recommend_id") BigInteger recommendId
            , @RequestParam(name = "add_image_count") Integer addImageCount
            , String[] deleteImageUrl) {
        return recommendService.patchImage(principalDetails.getMember(), recommendId, addImageCount, deleteImageUrl, amazonS3Service);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천서 삭제 성공"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "해당 유저의 디자이너의 프로필이 없습니다"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "잘못된 추천서 ID 를 입력받았습니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "503", description = "삭제할 권한이 없는 유저입니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "스타일 추천서 삭제")
    @DeleteMapping("")
    public ResponseEntity<ResultDto> delete(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "recommend_id") BigInteger recommendId) {
        return recommendService.delete(principalDetails.getMember(), recommendId);
    }
}
