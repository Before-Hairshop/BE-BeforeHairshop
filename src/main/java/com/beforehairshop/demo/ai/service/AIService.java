package com.beforehairshop.demo.ai.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.beforehairshop.demo.ai.domain.VirtualMemberImage;
import com.beforehairshop.demo.ai.model.MessagePayload;
import com.beforehairshop.demo.ai.repository.VirtualMemberImageRepository;
import com.beforehairshop.demo.aws.handler.CloudFrontUrlHandler;
import com.beforehairshop.demo.aws.service.AmazonS3Service;
import com.beforehairshop.demo.constant.ai.InferenceStatusKind;
import com.beforehairshop.demo.constant.member.StatusKind;
import com.beforehairshop.demo.fcm.service.FCMService;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.repository.MemberRepository;
import com.beforehairshop.demo.recommend.dto.RecommendDto;
import com.beforehairshop.demo.response.ResultDto;
import com.google.firebase.messaging.FirebaseMessagingException;
import jdk.jshell.Snippet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigInteger;

import static com.beforehairshop.demo.log.LogFormat.makeErrorLog;
import static com.beforehairshop.demo.log.LogFormat.makeSuccessLog;
import static com.beforehairshop.demo.response.ResultDto.makeResult;

@Service
@Slf4j
public class AIService {
    private final VirtualMemberImageRepository virtualMemberImageRepository;
    private final MemberRepository memberRepository;
    private final CloudFrontUrlHandler cloudFrontUrlHandler;

    private final QueueMessagingTemplate queueMessagingTemplate;
    private final MessageSender messageSender;
    private final FCMService fcmService;



    @Autowired
    public AIService(VirtualMemberImageRepository virtualMemberImageRepository, AmazonSQS amazonSqs, MessageSender messageSender, FCMService fcmService, MemberRepository memberRepository, CloudFrontUrlHandler cloudFrontUrlHandler) {
        this.virtualMemberImageRepository = virtualMemberImageRepository;
        this.queueMessagingTemplate = new QueueMessagingTemplate((AmazonSQSAsync) amazonSqs);
        this.messageSender = messageSender;
        this.fcmService = fcmService;
        this.memberRepository = memberRepository;
        this.cloudFrontUrlHandler = cloudFrontUrlHandler;
    }



    @Transactional
    public ResponseEntity<ResultDto> inference(Member member, BigInteger virtualMemberImageId) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");

        VirtualMemberImage virtualMemberImage = virtualMemberImageRepository.findById(virtualMemberImageId).orElse(null);
        if (virtualMemberImage == null)
            return makeResult(HttpStatus.NOT_FOUND, "해당 ID를 가지는 유저의 이미지는 없습니다.");

        if (virtualMemberImage.getInferenceStatus().equals(InferenceStatusKind.SUCCESS.getId()))
            return makeResult(HttpStatus.CONFLICT, "이미 추론이 완료된 프로필입니다.");
        else if (virtualMemberImage.getInferenceStatus().equals(InferenceStatusKind.FAIL.getId()))
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "추론이 불가능한 프로필입니다");

        sendMessageToRequestQueue(member.getId(), virtualMemberImageId);

        return makeResult(HttpStatus.OK, "추론 요청 성공");
    }

    private void sendMessageToRequestQueue(BigInteger memberId, BigInteger memberImageId) {
        messageSender.send(new MessagePayload(InferenceStatusKind.WAIT.getTitle(), memberId, memberImageId));
    }

    @Transactional
    public ResponseEntity<ResultDto> saveVirtualMemberImage(Member member, AmazonS3Service amazonS3Service) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");

        VirtualMemberImage virtualMemberImage = VirtualMemberImage.builder()
                .imageUrl(null)
                .inferenceStatus(InferenceStatusKind.WAIT.getId())
                .status(StatusKind.NORMAL.getId())
                .build();

        virtualMemberImage = virtualMemberImageRepository.save(virtualMemberImage);

        String preSignedUrl = amazonS3Service.generatePreSignedUrl(
                cloudFrontUrlHandler.getVirtualMemberImageS3Path(member.getId(), virtualMemberImage.getId())
        );

        virtualMemberImage.setImageUrl(
                cloudFrontUrlHandler.getVirtualMemberImageUrl(member.getId(), virtualMemberImage.getId())
        );

        return makeResult(HttpStatus.OK, preSignedUrl);
    }

    @Transactional
    public ResponseEntity<ResultDto> premiumInference(Member member, BigInteger virtualMemberImageId) {
        return null;
    }

    @Transactional
    public void processByInferenceResult(MessagePayload messagePayload) {

        log.info(messagePayload.toString());

        Member member = memberRepository.findById(messagePayload.getMemberId()).orElse(null);

        VirtualMemberImage virtualMemberImage = virtualMemberImageRepository.findByIdAndInferenceStatus(
                messagePayload.getVirtualMemberImageId(), InferenceStatusKind.WAIT.getId()
        ).orElse(null);
        if (member == null || virtualMemberImage == null) return;

        if (messagePayload.getResult().equals("fail")) {
            virtualMemberImage.setInferenceStatus(InferenceStatusKind.FAIL.getId());

            // FCM fail notification
            try {
                sendFCMMessageToMemberByFailInference(member.getDeviceToken());
            } catch (Exception e) {
                log.error("[POST] /api/v1/virtual_hairstyling/inference - fail 푸시알림 실패");
            }

        }
        else if (messagePayload.getResult().equals("success")) {
            virtualMemberImage.setInferenceStatus(InferenceStatusKind.SUCCESS.getId());

            // FCM success notification
            try {
                sendFCMMessageToMemberBySuccessInference(member.getDeviceToken());
            } catch (Exception e) {
                log.error("[POST] /api/v1/virtual_hairstyling/inference - fail 푸시알림 실패");
            }
        }

    }

    @Transactional
    public ResponseEntity<ResultDto> testSqs(BigInteger memberId, BigInteger virtualMemberImageId) {
        sendMessageToRequestQueue(memberId, virtualMemberImageId);
        return makeResult(HttpStatus.OK, "성공");
    }

    private void sendFCMMessageToMemberByFailInference(String memberDeviceToken) throws FirebaseMessagingException, IOException {
        fcmService.sendMessageTo(memberDeviceToken, "AI 가 사용자의 얼굴을 인식하지 못했습니다", "다른 얼굴 이미지로 테스트해보세요!");
    }

    private void sendFCMMessageToMemberBySuccessInference(String memberDeviceToken) throws FirebaseMessagingException, IOException {
        fcmService.sendMessageTo(memberDeviceToken, "AI 가 가상 헤어스타일링 이미지를 생성했습니다!", "가상 헤어스타일링 결과를 보고 어울리는 머리를 찾아보세요!");
    }

    @Transactional
    public ResponseEntity<ResultDto> deleteVirtualMemberImage(Member member, String virtualMemberImageUrl) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");

        VirtualMemberImage virtualMemberImage = virtualMemberImageRepository.findByImageUrlAndStatus(virtualMemberImageUrl
                , StatusKind.NORMAL.getId()).orElse(null);
        if (virtualMemberImage == null) {
            log.error(makeErrorLog(400, "/api/v1/virtual_hairstyling", "DELETE", "잘못된 이미지 url 입니다"));
            return makeResult(HttpStatus.BAD_REQUEST, "잘못된 이미지 url 입니다");
        }

        if (!virtualMemberImage.getMember().getId().equals(member.getId())) {
            log.error(makeErrorLog(503, "/api/v1/virtual_hairstyling", "DELETE", "삭제 권한 없음"));
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "삭제 권한이 없는 유저");
        }

        virtualMemberImageRepository.delete(virtualMemberImage);

        log.info(makeSuccessLog(200, "/api/v1/virtual_hairstyling", "DELETE", "삭제 성공"));
        return makeResult(HttpStatus.OK, "삭제 완료");
    }
}

