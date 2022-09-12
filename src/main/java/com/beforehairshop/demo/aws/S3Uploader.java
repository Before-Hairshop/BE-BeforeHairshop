package com.beforehairshop.demo.aws;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Uploader {

    public static final String CLOUD_FRONT_DOMAIN_NAME = "https://d8ov181gler1c.cloudfront.net";

    private final AmazonS3Client amazonS3Client;


    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    public String upload(MultipartFile multipartFile, String fileName) throws IOException {
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, multipartFile.getInputStream(), null).withCannedAcl(CannedAccessControlList.PublicRead));
        return CLOUD_FRONT_DOMAIN_NAME + "/" + fileName;

    }

    public void delete(String currentFilePath){
        if ("".equals(currentFilePath) == false && currentFilePath != null) {
            boolean isExistObject = amazonS3Client.doesObjectExist(bucket, currentFilePath);

            if (isExistObject) {
                amazonS3Client.deleteObject(bucket, currentFilePath);
            }
        }
    }

    private String putS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }
}