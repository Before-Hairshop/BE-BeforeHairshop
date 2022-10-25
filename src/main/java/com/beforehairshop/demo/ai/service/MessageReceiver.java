package com.beforehairshop.demo.ai.service;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.beforehairshop.demo.ai.model.MessagePayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageReceiver {

    private static final ObjectMapper OBJECT_MAPPER = Jackson2ObjectMapperBuilder.json().build();
    private final AIService aiService;

//    public MessageReceiver(AIService aiService) {
//        this.aiService = aiService;
//    }


    @SqsListener(value = "${cloud.aws.sqs.response-queue.name}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void processMessage(String message) {
        try {
            log.debug("Received new SQS message: {}", message);
            MessagePayload messagePayload = OBJECT_MAPPER.readValue(message, MessagePayload.class);

            this.aiService.processByInferenceResult(messagePayload);

        } catch (Exception e) {
            throw new RuntimeException("Cannot process message from SQS", e);
        }
    }

//    @SqsListener(value = "testObjectQueue", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS )
//    public void receiveObjectMessage(final MessagePayload messagePayload,
//                                     @Header("SenderId") String senderId) {
//        log.info("object message received {} {}", senderId, messagePayload);
//    }
}
