package com.beforehairshop.demo.ai.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.beforehairshop.demo.ai.model.MessagePayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessageChannel;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Component
public class MessageSender {
    private final QueueMessagingTemplate queueMessagingTemplate;

    @Value("${cloud.aws.sqs.request-queue.name}")
    private String requestQueueName;

    @Autowired
    public MessageSender(AmazonSQS amazonSQS) {
        this.queueMessagingTemplate = new QueueMessagingTemplate((AmazonSQSAsync) amazonSQS);
    }

    public <T> void send(T message) {
        if (message == null) {
            log.warn("SQS Producer cant produce 'null' value");
            return;
        }

        log.debug(" Messgae {} " + message);
        log.debug(" Queue name {} " + requestQueueName);
        queueMessagingTemplate.convertAndSend(requestQueueName, message);
    }

}
