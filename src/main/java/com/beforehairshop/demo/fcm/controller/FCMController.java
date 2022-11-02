package com.beforehairshop.demo.fcm.controller;

import com.beforehairshop.demo.fcm.service.FCMService;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerProfileDto;
import com.beforehairshop.demo.response.ResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "FCM 푸시 알림 관련 Controller")
@AllArgsConstructor
@RequestMapping("/api/v1/fcm")
public class FCMController {

    private final FCMService fcmService;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "본인 프로필(디자이너) 조회 성공"
                    , content = @Content(schema = @Schema(implementation = HairDesignerProfileDto.class)))
    })
    @Operation(summary = "[디자이너에게] 스타일 추천 요청서 작성 시, 디자이너에게 푸시 알림 전송")
    @PostMapping("send/to_designer/saving_recommend_request")
    public ResponseEntity<ResultDto> sendMessageToDesignerBySavingRecommendRequest(@RequestParam(name = "memberName") String memberName
            , @RequestParam(name = "designerDeviceToken") String designerDeviceToken) {
        return fcmService.sendFCMMessageToDesignerBySavingRecommendRequest(designerDeviceToken, memberName);
    }

    @Operation(summary = "[디자이너에게] 스타일 추천서를 수락할 시, 디자이너에게 푸시 알림 전송")
    @PostMapping("send/to_designer/accept_recommend")
    public ResponseEntity<ResultDto> sendMessageToDesignerByAcceptRecommend(@RequestParam(name = "memberName") String memberName
            , @RequestParam(name = "designerDeviceToken") String designerDeviceToken) {
        return fcmService.sendFCMMessageToDesignerByAcceptRecommend(designerDeviceToken, memberName);
    }

    @Operation(summary = "[유저에게] 스타일 추천서 보낼 때, 유저에게 푸시 알림 전송")
    @PostMapping("send/to_member/saving_recommend")
    public ResponseEntity<ResultDto> sendFCMMessageToMemberBySavingRecommend(@RequestParam(name = "designer_name") String designerName
            , @RequestParam(name = "member_device_token") String memberDeviceToken) {
        return fcmService.sendFCMMessageToMemberBySavingRecommend(memberDeviceToken, designerName);
    }


}
