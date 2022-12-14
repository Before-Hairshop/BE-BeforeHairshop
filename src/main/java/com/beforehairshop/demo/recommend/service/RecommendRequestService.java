package com.beforehairshop.demo.recommend.service;

import com.beforehairshop.demo.constant.member.StatusKind;
import com.beforehairshop.demo.fcm.service.FCMService;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.hairdesigner.repository.HairDesignerProfileRepository;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfile;
import com.beforehairshop.demo.member.dto.MemberProfileDto;
import com.beforehairshop.demo.member.repository.MemberProfileRepository;
import com.beforehairshop.demo.member.repository.MemberRepository;
import com.beforehairshop.demo.recommend.domain.RecommendRequest;
import com.beforehairshop.demo.recommend.dto.RecommendRequestDto;
import com.beforehairshop.demo.recommend.dto.post.RecommendRequestSaveRequestDto;
import com.beforehairshop.demo.recommend.repository.RecommendRequestRepository;
import com.beforehairshop.demo.response.ResultDto;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.beforehairshop.demo.response.ResultDto.makeResult;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendRequestService {

    private final RecommendRequestRepository recommendRequestRepository;
    private final MemberRepository memberRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final HairDesignerProfileRepository hairDesignerProfileRepository;
    private final FCMService fcmService;


    @Transactional
    public ResponseEntity<ResultDto> save(Member member, RecommendRequestSaveRequestDto saveRequestDto) throws IOException, FirebaseMessagingException {
        if (member == null) {
            log.error("[POST] /api/v1/recommend/request - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        if (!saveDtoIsValid(saveRequestDto)) {
            log.error("[POST] /api/v1/recommend/request - 204 (???????????? ?????? ?????? ??????)");
            return makeResult(HttpStatus.NO_CONTENT, "????????? ?????? ???????????? ???????????? ?????? ????????? ????????????.");
        }
        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(
                member, StatusKind.NORMAL.getId()
        ).orElse(null);

        if (memberProfile == null) {
            log.error("[POST] /api/v1/recommend/request - 503 (???????????? ???????????? ?????? ??????, ????????? ??? ??????)");
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "?????? ???????????? ???????????? ?????? ?????? ????????? ?????? ???????????? ????????? ??? ??????.");
        }
        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByIdAndStatus(
                saveRequestDto.getHairDesignerProfileId(), StatusKind.NORMAL.getId()
        ).orElse(null);

        if (hairDesignerProfile == null) {
            log.error("[POST] /api/v1/recommend/request - 400 (????????? ????????? ID)");
            return makeResult(HttpStatus.BAD_REQUEST, "????????? ???????????? ????????? ID ??? ???????????????.");
        }
        RecommendRequest checkRequest = recommendRequestRepository.findByToRecommendRequestProfileAndFromRecommendRequestProfileAndStatus(
                hairDesignerProfile, memberProfile, StatusKind.NORMAL.getId()
        ).orElse(null);

        // ?????? ?????? ???????? ????????? ??????, ????????? ???????????? ?????? ???????????? ?????? ????????? ?????? ??????... ???,, ?????? ??? ???
        if (checkRequest != null) {
            log.error("[POST] /api/v1/recommend/request - 409 (?????? ?????? ???????????? ?????????)");
            return makeResult(HttpStatus.CONFLICT, "?????? ?????? ???????????? ?????????.");
        }

        RecommendRequest recommendRequest = new RecommendRequest(hairDesignerProfile, memberProfile, StatusKind.NORMAL.getId());
        recommendRequest = recommendRequestRepository.save(recommendRequest);

        hairDesignerProfile.addToRecommendRequest(recommendRequest);
        memberProfile.addFromRecommendRequest(recommendRequest);

        try {
            sendFCMMessageToDesignerBySavingRecommendRequest(hairDesignerProfile.getHairDesigner().getDeviceToken()
                    , memberProfile.getName()
                    , hairDesignerProfile.getHairDesigner().getId());
        } catch (Exception e) {
            log.error("[POST] /api/v1/recommend/request - ?????? ?????? ??????");
            return makeResult(HttpStatus.OK, new RecommendRequestDto(recommendRequest));
        }

        return makeResult(HttpStatus.OK, new RecommendRequestDto(recommendRequest));
    }

    private void sendFCMMessageToDesignerBySavingRecommendRequest(String designerDeviceToken, String memberName, BigInteger designerId) throws FirebaseMessagingException, IOException {

        fcmService.sendMessageTo(designerDeviceToken, "????????? ????????? ???????????? ????????? ????????????.", "'" + memberName + "' ?????? ????????????????????? ????????? ???????????? ?????????????????????. ??????????????????!");

//        try {
//            fcmService.sendMessageTo(designerDeviceToken, "???????????????", "'" + memberName + "' ?????? ????????????????????? ????????? ???????????? ?????????????????????. ??????????????????!");
//        }
//        catch (IOException exception) {
//            log.error("[POST] /api/v1/recommend/request - FCM push notification fail (member id : " + designerId + ")");
//            log.error(exception.getStackTrace().toString());
//        }
    }

    private boolean saveDtoIsValid(RecommendRequestSaveRequestDto saveRequestDto) {
        return saveRequestDto.getHairDesignerProfileId() != null;
    }

    @Transactional
    public ResponseEntity<ResultDto> delete(Member member, BigInteger recommendRequestId) {
        if (member == null) {
            log.error("[DEL] /api/v1/recommend/request - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        RecommendRequest recommendRequest = recommendRequestRepository.findByIdAndStatus(recommendRequestId, StatusKind.NORMAL.getId()).orElse(null);
        if (recommendRequest == null) {
            log.error("[DEL] /api/v1/recommend/request - 404 (????????? ?????? ????????? ID)");
            return makeResult(HttpStatus.NOT_FOUND, "????????? ?????? ????????? ID ?????????.");
        }
        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null) {
            log.error("[DEL] /api/v1/recommend/request - 404 (???????????? ???????????? ?????????)");
            return makeResult(HttpStatus.NOT_FOUND, "?????? ???????????? ???????????? ????????????.");
        }
        if (!recommendRequest.getFromRecommendRequestProfile().getId().equals(memberProfile.getId())) {
            log.error("[DEL] /api/v1/recommend/request - 503 (?????? ???????????? ????????? ????????? ?????? ??????)");
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "?????? ?????? ???????????? ????????? ????????? ?????? ???????????????.");
        }
        recommendRequestRepository.delete(recommendRequest);

        return makeResult(HttpStatus.OK, "?????? ??????");
    }

    @Transactional
    public ResponseEntity<ResultDto> findManyByDesigner(Member member) {
        if (member == null) {
            log.error("[GET] /api/v1/recommend/request/list_by_designer - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }

        if (member.getDesignerFlag() != 1 || !member.getRole().equals("ROLE_DESIGNER")) {
            log.error("[GET] /api/v1/recommend/request/list_by_designer - 503 (?????? ??????????????? ??????) : member_id = " + member.getId());
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "?????? ????????? ?????? ??????????????? ????????????");
        }
        HairDesignerProfile hairDesignerProfile
                = hairDesignerProfileRepository.findByHairDesignerAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (hairDesignerProfile == null) {
            log.error("[GET] /api/v1/recommend/request/list_by_designer - 404 (?????? ???????????? ???????????? ???????????? ?????? ????????????) : member_id = " + member.getId());
            return makeResult(HttpStatus.NOT_FOUND, "???????????? ???????????? ???????????? ?????? ????????????.");
        }

        if (hairDesignerProfile.getToRecommendRequestSet() == null) {
            return makeResult(HttpStatus.OK, null);
        }

        List<RecommendRequest> recommendRequestList = recommendRequestRepository.findByToRecommendRequestProfileAndStatusOrderByCreateDateDesc(
                hairDesignerProfile, StatusKind.NORMAL.getId()
        );

        List<MemberProfileDto> memberProfileDtoList = recommendRequestList.stream()
                .map(RecommendRequest::getFromRecommendRequestProfile).collect(Collectors.toList())
                .stream()
                .map(MemberProfileDto::new).collect(Collectors.toList());

//        List<MemberProfileDto> memberProfileDtoList = hairDesignerProfile.getToRecommendRequestSet().stream()
//                .map(RecommendRequest::getFromRecommendRequestProfile)
//                .collect(Collectors.toList())
//                .stream().map(MemberProfileDto::new).collect(Collectors.toList());

        // ?????? ????????? 1??????, ??????????????? ?????????, ?????????????????? ????????? ???!

        return makeResult(HttpStatus.OK, memberProfileDtoList);

    }
}
