package com.beforehairshop.demo.aws.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;

@Service
@Slf4j
@RequiredArgsConstructor
public class AmazonS3Service {
    @Value("${cloud.aws.s3.bucket}")
    private final String S3BucketName;

    private final AmazonS3Client amazonS3Client;

    public String generatePreSignedUrl(String objectPath) {

        String preSignedURL = null;

        try {
            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(S3BucketName, objectPath)
                            .withMethod(HttpMethod.GET);
            URL url = amazonS3Client.generatePresignedUrl(generatePresignedUrlRequest);
            preSignedURL = url.toString();

            log.info("Generate Presigned_Url - " + preSignedURL);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return preSignedURL;
    }
}
