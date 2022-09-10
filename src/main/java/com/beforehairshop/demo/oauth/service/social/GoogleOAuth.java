package com.beforehairshop.demo.oauth.service.social;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class GoogleOAuth {

//
//    @Override
//    public String getOauthRedirectURL() {
//        Map<String, Object> params = new HashMap<>();
//        params.put("scope", "email");
//        params.put("access_type", "offline");
//        //params.put("include_granted_scopes", "true");
//        params.put("response_type", "code");
//        params.put("client_id", getGoogleSnsClientId());
//        params.put("redirect_uri", getGoogleSnsCallbackUrl());
//
//        String parameterString = params.entrySet().stream()
//                .map(x -> x.getKey() + "=" + x.getValue())
//                .collect(Collectors.joining("&"));
//
//        System.out.println("요청 rediretURL : " + getGoogleSnsBaseUrl() + "?" + parameterString);
//        return getGoogleSnsBaseUrl() + "?" + parameterString;
//    }
//
//    @Override
//    public String requestAccessToken(String code) {
//        RestTemplate restTemplate = new RestTemplate();
//
//        Map<String, Object> params = new HashMap<>();
//        params.put("code", code);
//        params.put("client_id", getGoogleSnsClientId());
//        params.put("client_secret", getGoogleSnsClientSecret());
//        params.put("redirect_uri", getGoogleSnsCallbackUrl());
//        params.put("grant_type", "authorization_code");
//
//        ResponseEntity<String> responseEntity =
//                restTemplate.postForEntity(getGoogleSnsTokenBaseUrl(), params, String.class);
//
//        if (responseEntity.getStatusCode() == HttpStatus.OK) {
//            return responseEntity.getBody();
//        }
//        return "구글 로그인 요청 처리 실패";
//    }
//
//    @Override
//    public String requestEmail(String accessToken) {
//        String emailRequestUrl = "https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + accessToken;
//        RestTemplate restTemplate = new RestTemplate();
//
//        ResponseEntity<String> responseEntity =
//                restTemplate.getForEntity(emailRequestUrl, String.class);
//
//        if (responseEntity.getStatusCode() == HttpStatus.OK) {
//            return responseEntity.getBody();
//        }
//        return "이메일 가져오기 실패";
//    }
//
////    public String getUserInfo(String code) {
////        String RequestUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
////
////        final HttpClient client = HttpClientBuilder.create().build();
////        final HttpGet get = new HttpGet(RequestUrl);
////
////        JsonNode returnNode = null;
////
////        // add header
////        get.addHeader("Authorization", "Bearer " + autorize_code);
////
////        try {
////            final HttpResponse response = client.execute(get);
////            final int responseCode = response.getStatusLine().getStatusCode();
////
////            ObjectMapper mapper = new ObjectMapper();
////            returnNode = mapper.readTree(response.getEntity().getContent());
////
////            System.out.println("\nSending 'GET' request to URL : " + RequestUrl);
////            System.out.println("Response Code : " + responseCode);
////
////
////        } catch (UnsupportedEncodingException e) {
////            e.printStackTrace();
////        } catch (ClientProtocolException e) {
////            e.printStackTrace();
////        } catch (IOException e) {
////            e.printStackTrace();
////        } finally {
////            // clear resources
////        }
////        return returnNode;
////    }
//
//    public String requestAccessTokenUsingURL(String code) {
//        try {
//            URL url = new URL(getGoogleSnsTokenBaseUrl());
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            conn.setDoOutput(true);
//
//            Map<String, Object> params = new HashMap<>();
//            params.put("code", code);
//            params.put("client_id", getGoogleSnsClientId());
//            params.put("client_secret", getGoogleSnsClientSecret());
//            params.put("redirect_uri", getGoogleSnsCallbackUrl());
//            params.put("grant_type", "authorization_code");
//
//            String parameterString = params.entrySet().stream()
//                    .map(x -> x.getKey() + "=" + x.getValue())
//                    .collect(Collectors.joining("&"));
//
//            BufferedOutputStream bous = new BufferedOutputStream(conn.getOutputStream());
//            bous.write(parameterString.getBytes());
//            bous.flush();
//            bous.close();
//
//            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//
//            StringBuilder sb = new StringBuilder();
//            String line;
//
//            while ((line = br.readLine()) != null) {
//                sb.append(line);
//            }
//
//            if (conn.getResponseCode() == 200) {
//                return sb.toString();
//            }
//            return "구글 로그인 요청 처리 실패";
//        } catch (IOException e) {
//            throw new IllegalArgumentException("알 수 없는 구글 로그인 Access Token 요청 URL 입니다 :: " + getGoogleSnsTokenBaseUrl());
//        }
//    }
}