package com.beforehairshop.demo.oauth.service.social;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.beforehairshop.demo.secret.social.SecretKakao.*;
import static com.beforehairshop.demo.secret.social.SecretKakao.getKakaoSnsClientId;

@Component
@Slf4j
public class KakaoOAuth implements SocialOAuth {

    @Override
    public String getOauthRedirectURL() {
        Map<String, Object> params = new HashMap<>();
        params.put("client_id", getKakaoSnsClientId());
        params.put("redirect_uri", getOauthRedirectURL());
        params.put("response_type", "code");

        String parameterString = params.entrySet().stream()
                .map(x -> x.getKey() + "=" + x.getValue())
                .collect(Collectors.joining("&"));

        return getKakaoSnsBaseUrl() + "?" + parameterString;
    }

    @Override
    public String requestAccessToken(String code) {
        String reqURL = getKakaoSnsTokenBaseUrl();

        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("grant_type=authorization_code");
            sb.append("&client_id=" + getKakaoSnsClientId());
            sb.append("&redirect_uri=" + getKakaoSnsRedirectUri());
            sb.append("&code=" + code);
            bw.write(sb.toString());
            bw.flush();

            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }

            System.out.println("response body : " + result);

            br.close();
            bw.close();

            if (responseCode == 200) {
                return result;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "카카오 로그인 요청 처리 실패";
    }

    @Override
    public String requestEmail(String accessToken) {
        String reqURL = getKakaoSnsRequestBaseUrl();

        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }

            System.out.println("response body : " + result);

            br.close();

            if(responseCode == 200) {
                return result;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "이메일 가져오기 실패";
    }
}
