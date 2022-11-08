package com.beforehairshop.demo.ai.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.beforehairshop.demo.ai.domain.VirtualMemberImage;
import com.beforehairshop.demo.ai.model.MessagePayload;
import com.beforehairshop.demo.ai.repository.VirtualMemberImageRepository;
import com.beforehairshop.demo.constant.ai.InferenceStatusKind;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.response.ResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

import static com.beforehairshop.demo.response.ResultDto.makeResult;

@Service
@Slf4j
public class AIService {
    private final VirtualMemberImageRepository virtualMemberImageRepository;

    private final QueueMessagingTemplate queueMessagingTemplate;
    private final MessageSender messageSender;



    @Autowired
    public AIService(VirtualMemberImageRepository virtualMemberImageRepository, AmazonSQS amazonSqs, MessageSender messageSender) {
        this.virtualMemberImageRepository = virtualMemberImageRepository;
        this.queueMessagingTemplate = new QueueMessagingTemplate((AmazonSQSAsync) amazonSqs);
        this.messageSender = messageSender;
    }



    @Transactional
    public ResponseEntity<ResultDto> inference(Member member, BigInteger memberImageId) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");

        VirtualMemberImage virtualMemberImage = virtualMemberImageRepository.findById(memberImageId).orElse(null);
        if (virtualMemberImage == null)
            return makeResult(HttpStatus.NOT_FOUND, "해당 ID를 가지는 유저의 이미지는 없습니다.");

        if (virtualMemberImage.getInferenceStatus().equals(InferenceStatusKind.SUCCESS.getId()))
            return makeResult(HttpStatus.CONFLICT, "이미 추론이 완료된 프로필입니다.");
        else if (virtualMemberImage.getInferenceStatus().equals(InferenceStatusKind.FAIL.getId()))
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "추론이 불가능한 프로필입니다");

        sendMessageToRequestQueue(member.getId(), memberImageId);

        return makeResult(HttpStatus.OK, "추론 요청 성공");
    }

    private void sendMessageToRequestQueue(BigInteger memberId, BigInteger memberImageId) {
        messageSender.send(new MessagePayload(InferenceStatusKind.WAIT.getTitle(), memberId, memberImageId));
    }

    @Transactional
    public ResponseEntity<ResultDto> saveMemberImage(Member member) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");

        return null;
    }

    @Transactional
    public ResponseEntity<ResultDto> premiumInference(Member member, BigInteger memberImageId) {
        return null;
    }

    @Transactional
    public void processByInferenceResult(MessagePayload messagePayload) {

        log.info(messagePayload.toString());
        VirtualMemberImage virtualMemberImage = virtualMemberImageRepository.findByIdAndInferenceStatus(
                messagePayload.getMemberImageId(), InferenceStatusKind.WAIT.getId()
        ).orElse(null);
        if (virtualMemberImage == null) return;

        if (messagePayload.getResult().equals("fail")) {
            virtualMemberImage.setInferenceStatus(InferenceStatusKind.FAIL.getId());
            /**
             * FCM 통해서, 실패 알림 보내기
             */
        }
        else if (messagePayload.getResult().equals("success")) {
            virtualMemberImage.setInferenceStatus(InferenceStatusKind.SUCCESS.getId());
            /**
             * FCM 통해서, 성공 알림 보내기
             */
        }

    }

    @Transactional
    public ResponseEntity<ResultDto> testSqs(BigInteger memberId, BigInteger memberImageId) {
        sendMessageToRequestQueue(memberId, memberImageId);
        return makeResult(HttpStatus.OK, "성공");
    }
}

