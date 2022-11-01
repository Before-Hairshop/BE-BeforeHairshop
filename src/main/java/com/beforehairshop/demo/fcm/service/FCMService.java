package com.beforehairshop.demo.fcm.service;

import com.beforehairshop.demo.aws.handler.CloudFrontUrlHandler;
import com.beforehairshop.demo.fcm.dto.FCMMessage;
import com.beforehairshop.demo.response.ResultDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.net.HttpHeaders;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.google.firebase.messaging.Message;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import static com.beforehairshop.demo.response.ResultDto.makeResult;

@Service
@Slf4j
@RequiredArgsConstructor
public class FCMService {


    private String API_URL = "https://fcm.googleapis.com/v1/projects/689635962511/messages:send";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CloudFrontUrlHandler cloudFrontUrlHandler;

    @PostConstruct
    public void initialize() throws IOException {
        String firebaseConfigPath = "firebase-service-key.json";
        String[] SCOPES = { "https://www.googleapis.com/auth/firebase.messaging" };

        log.info("[단계0] FCM 메시지 send 함수 호출");

        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                .createScoped(Arrays.asList(SCOPES));

        googleCredentials.refresh();

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(googleCredentials)
                .build();

        FirebaseApp.initializeApp(options, "beforehairshop");
    }

    public void sendMessageToV2(String targetToken, String title, String body) throws FirebaseMessagingException, IOException {
        String message = makeMessage(targetToken, title, body);
        log.info("[단계0] 알림 기능 - 알림으로 보내는 message 값 :" + message);
//        Map respMap = objectMapper.readValue(message, Map.class);
//
//        log.info("==[단계1] send message ==");

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(message, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .build();

       // log.info("[단계1] 알림 기능 - request 생성");

        Response response = client.newCall(request).execute();

      //  log.info("[단계2] 알림 기능 - response 로 받은 값 :" + response.body());

        log.info(response.body().string());
    }

    public void sendMessageTo(String targetToken, String title, String body) throws FirebaseMessagingException, IOException {


        //   See documentation on defining a message payload.
        Message message = Message.builder()
                .putData("title", title)
                .putData("body", body)
                .setToken(targetToken)
                .build();

        // log.info("[단계1] 메시지 생성");
        String response = FirebaseMessaging.getInstance(FirebaseApp.getInstance("beforehairshop")).send(message);
        log.info("FCM 메시지 보내기 성공");

       // log.info(response);
//        try {
//            String response = FirebaseMessaging.getInstance(FirebaseApp.getInstance("beforehairshop")).sendAsync(message).get();
//
//        } catch (ExecutionException e1) {
//            e1.printStackTrace();
//        } catch (InterruptedException e2) {
//            e2.printStackTrace();
//        }
//        log.info("[단계3] 메시지 보내고 response 받기");
    }

    // 파라미터를 FCM이 요구하는 body 형태로 만들어준다.
    private String makeMessage(String targetToken, String title, String body) throws JsonProcessingException {
        FCMMessage fcmMessage = FCMMessage.builder()
                .message(FCMMessage.Message.builder()
                        .token(targetToken)
                        .notification(FCMMessage.Notification.builder()
                                .title(title)
                                .body(body)
                                .image(null)
                                .build()
                        )
                        .build()
                )
                .validate_only(false)
                .build();

        return objectMapper.writeValueAsString(fcmMessage);
    }

//    private String getAccessToken() throws IOException {
//        String firebaseConfigPath = "firebase-service-key.json";
//
//        GoogleCredentials googleCredentials = GoogleCredentials
//                .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
//                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
//
//        googleCredentials.refreshIfExpired();
//
//        return googleCredentials.getAccessToken().getTokenValue();
//    }

    // [START retrieve_access_token]
    private String getAccessToken() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource("firebase-service-key.json").getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/firebase.messaging"));

        googleCredentials.refresh();
        //googleCredentials.refreshAccessToken();

        return googleCredentials.getAccessToken().getTokenValue();
    }
    // [END retrieve_access_token]

    /**
     * Create HttpURLConnection that can be used for both retrieving and publishing.
     *
     * @return Base HttpURLConnection.
     * @throws IOException
     */
    private HttpURLConnection getConnection() throws IOException {
        // [START use_access_token]
        URL url = new URL("https://fcm.googleapis.com/v1/projects/before-hairshop-ccec7/messages:send");
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty("Authorization", "Bearer " + getAccessToken());
        httpURLConnection.setRequestProperty("Content-Type", "application/json; UTF-8");
        return httpURLConnection;
        // [END use_access_token]
    }

    private void sendMessage(JsonObject fcmMessage) throws IOException {
        HttpURLConnection connection = getConnection();
        connection.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
        writer.write(fcmMessage.toString());
        writer.flush();
        writer.close();

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            String response = inputstreamToString(connection.getInputStream());
            System.out.println("Message sent to Firebase for delivery, response:");
            System.out.println(response);
        } else {
            System.out.println("Unable to send message to Firebase:");
            String response = inputstreamToString(connection.getErrorStream());
            System.out.println(response);
        }
    }

    public void sendCommonMessage(String title, String body) throws IOException {
        JsonObject notificationMessage = buildNotificationMessage(title, body);
        System.out.println("FCM request body for message using common notification object:");
        //prettyPrint(notificationMessage);
        sendMessage(notificationMessage);
    }



    private JsonObject buildNotificationMessage(String title, String body) {
        JsonObject jNotification = new JsonObject();
        jNotification.addProperty("title", title);
        jNotification.addProperty("body", body);
        jNotification.addProperty("image", cloudFrontUrlHandler.getLogoUrl());

        JsonObject jMessage = new JsonObject();
        jMessage.add("notification", jNotification);
        jMessage.addProperty("topic", "news");

        JsonObject jFcm = new JsonObject();
        jFcm.add("message", jMessage);

        return jFcm;
    }


    private String inputstreamToString(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNext()) {
            stringBuilder.append(scanner.nextLine());
        }
        return stringBuilder.toString();
    }

    public ResponseEntity<ResultDto> sendFCMMessageToDesignerBySavingRecommendRequest(String designerDeviceToken, String memberName) {
        try {
            sendMessageTo(designerDeviceToken, "스타일 추천을 요청하는 유저가 있습니다.", "'" + memberName + "' 님이 디자이너님에게 스타일 추천서를 요청하셨습니다. 확인해보세요!");
            return makeResult(HttpStatus.OK, "FCM 메시지 전송 성공");
        } catch (FirebaseMessagingException | IOException e) {
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "FCM 메시지 전송 실패");
        }
    }

    public ResponseEntity<ResultDto> sendFCMMessageToDesignerByAcceptRecommend(String designerDeviceToken, String memberName) {
        try {
            sendMessageTo(designerDeviceToken, "스타일 추천서가 수락되었습니다.",  memberName + " 님이 제안했던 스타일 추천서를 수락하셨습니다.");
            return makeResult(HttpStatus.OK, "FCM 메시지 전송 성공");
        } catch (FirebaseMessagingException | IOException e) {
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "FCM 메시지 전송 실패");
        }
    }

    public ResponseEntity<ResultDto> sendFCMMessageToMemberBySavingRecommend(String memberDeviceToken, String designerName) {
        try {
            sendMessageTo(memberDeviceToken, "스타일 추천서가 도착했습니다.", designerName + " 디자이너 님의 스타일 추천서가 도착했으니 확인해보세요.");
            return makeResult(HttpStatus.OK, "FCM 메시지 전송 성공");
        } catch (FirebaseMessagingException | IOException e) {
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "FCM 메시지 전송 실패");
        }
    }
}
