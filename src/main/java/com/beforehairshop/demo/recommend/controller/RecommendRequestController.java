package com.beforehairshop.demo.recommend.controller;

import com.amazonaws.Response;
import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.member.dto.MemberDto;
import com.beforehairshop.demo.member.dto.MemberProfileDto;
import com.beforehairshop.demo.recommend.domain.RecommendRequest;
import com.beforehairshop.demo.recommend.dto.RecommendRequestDto;
import com.beforehairshop.demo.recommend.dto.post.RecommendRequestSaveRequestDto;
import com.beforehairshop.demo.recommend.service.RecommendRequestService;
import com.beforehairshop.demo.response.ResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigInteger;

@RestController
@Tag(name = "스타일 추천서 요청 관련 Controller")
@RequiredArgsConstructor
@RequestMapping("/api/v1/recommend/request")
public class RecommendRequestController {

    private final RecommendRequestService recommendRequestService;

//    @Operation(summary = "스타일 추천 요청서 조회 API")
//    @GetMapping("")
//    public ResponseEntity<ResultDto> findOne(@AuthenticationPrincipal PrincipalDetails principalDetails
//            , @RequestParam(name = "recommendRequestId")BigInteger recommendRequestId) {
//        return recommendRequestService.findOne(principalDetails.getMember(), recommendRequestId);
//    }
//
//    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
//    @Operation(summary = "[유저] 내가 요청한 스타일 추천 요청서")
//    @GetMapping("list_by_user")
//    public ResponseEntity<ResultDto> findManyByUser(@AuthenticationPrincipal PrincipalDetails principalDetails) {
//        return recommendRequestService.findManyByUser(principalDetails.getMember());
//    }


    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "[디자이너] 내게 추천 요청서 작성한 유저들의 프로필 리스트 조회 성공"
                    , content = @Content(array = @ArraySchema(schema = @Schema(implementation = MemberProfileDto.class)))),
            @ApiResponse(responseCode = "404", description = "디자이너 프로필이 등록되어 있지 않다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "503", description = "해당 유저는 디자이너가 아닙니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('DESIGNER', 'ADMIN')")
    @Operation(summary = "[디자이너] 본인에게 추천서롤 요청한 유저들의 프로필 리스트")
    @GetMapping("list_by_designer")
    public ResponseEntity<ResultDto> findManyByDesigner(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return recommendRequestService.findManyByDesigner(principalDetails.getMember());
    }


    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스타일 추천 요청서 저장 성공"
                    , content = @Content(schema = @Schema(implementation = RecommendRequestDto.class))),
            @ApiResponse(responseCode = "204", description = "스타일 추천 요청서 저장에 필요한 정보가 부족하다"
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 디자이너 프로필 ID 가 입력되었다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "이미 해당 디자이너에게 요청서를 보냈습니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "503", description = "유저 프로필이 등록되어 있지 않아 스타일 추천 요청서를 작성할 수 없다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "스타일 추천 요청서 저장 API")
    @PostMapping("")
    public ResponseEntity<ResultDto> save(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestBody RecommendRequestSaveRequestDto saveRequestDto) throws IOException {
        return recommendRequestService.save(principalDetails.getMember(), saveRequestDto);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스타일 추천 요청서 삭제 성공"
                    , content = @Content(schema = @Schema(implementation = RecommendRequestDto.class))),
            @ApiResponse(responseCode = "404", description = "잘못된 추천 요청서 ID 입니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "503", description = "해당 추천 요청서를 삭제할 권한이 없는 유저입니다."
                    , content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "504", description = "세션 만료"
                    , content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "스타일 추천 요청서 삭제 API")
    @DeleteMapping("")
    public ResponseEntity<ResultDto> delete(@AuthenticationPrincipal PrincipalDetails principalDetails
            , @RequestParam(name = "recommendRequestId")BigInteger recommendRequestId) {
        return recommendRequestService.delete(principalDetails.getMember(), recommendRequestId);
    }


}
