package com.beforehairshop.demo.aws.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.cloud.aws.messaging.config.annotation.EnableSqs;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


//@EnableSqs
//@Configuration
public class SQSConfig {

//    @Value("${cloud.aws.region:ap-northeast-2}")
//    private String region;
//
////    @Value("${cloud.aws.credentials.accessKey}")
////    private String accessKey;
////
////    @Value("${cloud.aws.credentials.secretKey}")
////    private String secretKey;
//
//
////
////    private final String secretKey;
////
////    @Autowired
////    public SQSConfig(@Value("${cloud.aws.credentials.accessKey}")String accessKey, @Value("${cloud.aws.credentials.secretKey}")String secretKey, @Value("${cloud.aws.region}") String awsRegion) {
////        this.awsRegion = awsRegion;
////        this.accessKey = accessKey;
////        this.secretKey = secretKey;
////    }
////
////    private Environment env;
//
//
//    @Bean
//    @Primary
//    public AmazonSQSAsync amazonSQSAsync() {
//
//        return AmazonSQSAsyncClientBuilder.standard()
//                .withCredentials(credentialsProvider())
//                .withRegion(region)
//                .build();
//    }
//
//    @Bean
//    public AWSCredentialsProvider credentialsProvider() {
//        return new DefaultAWSCredentialsProviderChain();
//    }
//
//    @Bean
//    public QueueMessagingTemplate queueMessagingTemplate() {
//        return new QueueMessagingTemplate(amazonSQSAsync());
//    }
//
//
//    @Bean
//    public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory(AmazonSQSAsync amazonSQSAsync) {
//        SimpleMessageListenerContainerFactory factory = new SimpleMessageListenerContainerFactory();
//        factory.setAmazonSqs(amazonSQSAsync);
//        factory.setAutoStartup(true);
//        factory.setMaxNumberOfMessages(10);
//        factory.setTaskExecutor(createDefaultTaskExecutor());
//        return factory;
//    }
//
//    protected AsyncTaskExecutor createDefaultTaskExecutor() {
//        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
//        threadPoolTaskExecutor.setThreadNamePrefix("SQSExecutor - ");
//        threadPoolTaskExecutor.setCorePoolSize(100);
//        threadPoolTaskExecutor.setMaxPoolSize(100);
//        threadPoolTaskExecutor.setQueueCapacity(2);
//        threadPoolTaskExecutor.afterPropertiesSet();
//        return threadPoolTaskExecutor;
//    }
}
