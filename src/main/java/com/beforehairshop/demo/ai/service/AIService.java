package com.beforehairshop.demo.ai.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.beforehairshop.demo.ai.domain.VirtualMemberImage;
import com.beforehairshop.demo.ai.dto.VirtualMemberImagePostResponseDto;
import com.beforehairshop.demo.ai.dto.VirtualMemberImageResponseDto;
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
import com.google.api.Http;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");

        VirtualMemberImage virtualMemberImage = virtualMemberImageRepository.findById(virtualMemberImageId).orElse(null);
        if (virtualMemberImage == null)
            return makeResult(HttpStatus.NOT_FOUND, "?????? ID??? ????????? ????????? ???????????? ????????????.");

        if (virtualMemberImage.getInferenceStatus().equals(InferenceStatusKind.SUCCESS.getId()))
            return makeResult(HttpStatus.CONFLICT, "?????? ????????? ????????? ??????????????????.");
        else if (virtualMemberImage.getInferenceStatus().equals(InferenceStatusKind.FAIL.getId()))
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "????????? ???????????? ??????????????????");

        sendMessageToRequestQueue(member.getId(), virtualMemberImageId);

        return makeResult(HttpStatus.OK, "?????? ?????? ??????");
    }

    private void sendMessageToRequestQueue(BigInteger memberId, BigInteger memberImageId) {
        messageSender.send(new MessagePayload(InferenceStatusKind.WAIT.getTitle(), memberId, memberImageId));
    }

    @Transactional
    public ResponseEntity<ResultDto> saveVirtualMemberImage(Member member, AmazonS3Service amazonS3Service) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");

//        VirtualMemberImage virtualMemberImage = VirtualMemberImage.builder()
//                .member(member)
//                .imageUrl(null)
//                .inferenceStatus(InferenceStatusKind.WAIT.getId())
//                .status(StatusKind.NORMAL.getId())
//                .build();
//
//        virtualMemberImage = virtualMemberImageRepository.save(virtualMemberImage);
//
//        String preSignedUrl = amazonS3Service.generatePreSignedUrl(
//                cloudFrontUrlHandler.getVirtualMemberImageS3Path(member.getId(), virtualMemberImage.getId())
//        );
//
//        virtualMemberImage.setImageUrl(
//                cloudFrontUrlHandler.getVirtualMemberImageUrl(member.getId(), virtualMemberImage.getId())
//        );

        return makeResult(HttpStatus.SERVICE_UNAVAILABLE, new VirtualMemberImagePostResponseDto(BigInteger.valueOf(-1), "preSignedUrl"));
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
                log.error("[POST] /api/v1/virtual_hairstyling/inference - fail ???????????? ??????");
            }

        }
        else if (messagePayload.getResult().equals("success")) {
            virtualMemberImage.setInferenceStatus(InferenceStatusKind.SUCCESS.getId());

            // FCM success notification
            try {
                sendFCMMessageToMemberBySuccessInference(member.getDeviceToken());
            } catch (Exception e) {
                log.error("[POST] /api/v1/virtual_hairstyling/inference - fail ???????????? ??????");
            }
        }

    }

    @Transactional
    public ResponseEntity<ResultDto> testSqs(BigInteger memberId, BigInteger virtualMemberImageId) {
        sendMessageToRequestQueue(memberId, virtualMemberImageId);
        return makeResult(HttpStatus.OK, "??????");
    }

    private void sendFCMMessageToMemberByFailInference(String memberDeviceToken) throws FirebaseMessagingException, IOException {
        fcmService.sendMessageTo(memberDeviceToken, "AI ??? ???????????? ????????? ???????????? ???????????????", "?????? ?????? ???????????? ?????????????????????!");
    }

    private void sendFCMMessageToMemberBySuccessInference(String memberDeviceToken) throws FirebaseMessagingException, IOException {
        fcmService.sendMessageTo(memberDeviceToken, "AI ??? ?????? ?????????????????? ???????????? ??????????????????. ??????????????????!", "?????? ?????????????????? ????????? ?????? ???????????? ????????? ???????????????!");
    }

    @Transactional
    public ResponseEntity<ResultDto> deleteVirtualMemberImage(Member member, String virtualMemberImageUrl) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");

        VirtualMemberImage virtualMemberImage = virtualMemberImageRepository.findByImageUrlAndStatus(virtualMemberImageUrl
                , StatusKind.NORMAL.getId()).orElse(null);
        if (virtualMemberImage == null) {
            log.error(makeErrorLog(400, "/api/v1/virtual_hairstyling", "DELETE", "????????? ????????? url ?????????"));
            return makeResult(HttpStatus.BAD_REQUEST, "????????? ????????? url ?????????");
        }

        if (!virtualMemberImage.getMember().getId().equals(member.getId())) {
            log.error(makeErrorLog(503, "/api/v1/virtual_hairstyling", "DELETE", "?????? ?????? ??????"));
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "?????? ????????? ?????? ??????");
        }

        virtualMemberImageRepository.delete(virtualMemberImage);

        log.info(makeSuccessLog(200, "/api/v1/virtual_hairstyling", "DELETE", "?????? ??????"));
        return makeResult(HttpStatus.OK, "?????? ??????");
    }

    @Transactional
    public ResponseEntity<ResultDto> getMyVirtualMemberImageList(Member member) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");

        List<VirtualMemberImage> virtualMemberImageList
                = virtualMemberImageRepository.findByMemberAndStatusOrderByCreateDateAsc(member, StatusKind.NORMAL.getId());

        List<VirtualMemberImageResponseDto> virtualMemberImageResponseDtoList = virtualMemberImageList.stream()
                .map(VirtualMemberImageResponseDto::new)
                .collect(Collectors.toList());

        return makeResult(HttpStatus.OK, virtualMemberImageResponseDtoList);
    }

    public ResponseEntity<ResultDto> inferenceTest(BigInteger memberId, BigInteger virtualMemberImageId) {
        sendMessageToRequestQueue(memberId, virtualMemberImageId);

        return makeResult(HttpStatus.OK, "?????? ?????? ??????");
    }

    @Transactional
    public ResponseEntity<ResultDto> getInferenceResultList(Member member, BigInteger virtualMemberImageId) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");

        VirtualMemberImage virtualMemberImage = virtualMemberImageRepository.findById(virtualMemberImageId).orElse(null);
        if (virtualMemberImage == null)
            return makeResult(HttpStatus.BAD_REQUEST, "????????? ?????? ????????? ID ?????????.");

        if (!virtualMemberImage.getMember().getId().equals(member.getId())) {
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "????????? ????????? ?????? ???????????????");
        }

        if (!virtualMemberImage.getInferenceStatus().equals(InferenceStatusKind.SUCCESS.getId())) {
            return makeResult(HttpStatus.NOT_IMPLEMENTED, "inference ?????? ?????? ??????????????????");
        }

        List<String> resultImageList = new ArrayList<>();
        for (int referenceImageNumber = 1; referenceImageNumber < 6; referenceImageNumber++) {
            resultImageList.add(
                    cloudFrontUrlHandler.getInferenceResultImageUrl(member.getId(), virtualMemberImageId, referenceImageNumber)
            );
        }

        return makeResult(HttpStatus.OK, resultImageList);
    }

    @Transactional
    public ResponseEntity<ResultDto> getPreInferenceResultList(Member member, Integer preInputImageId) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");

        List<String> resultImageList = new ArrayList<>();
        for (int referenceImageNumber = 1; referenceImageNumber < 6; referenceImageNumber++) {
            resultImageList.add(
                    cloudFrontUrlHandler.getPreInferenceResultImageUrl(preInputImageId, referenceImageNumber)
            );
        }

        return makeResult(HttpStatus.OK, resultImageList);
    }
}

