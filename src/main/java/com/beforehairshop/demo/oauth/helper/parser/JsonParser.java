package com.beforehairshop.demo.oauth.helper.parser;

import com.beforehairshop.demo.oauth.dto.OAuthDto;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@Slf4j
public class JsonParser {

    public static OAuthDto parseGoogleTokenInfo(String tokenInfoFromGoogle) {
        OAuthDto oAuthDto = new OAuthDto();
        JSONParser jsonParser = new JSONParser();

        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(tokenInfoFromGoogle);
            oAuthDto.setAccessToken((String) jsonObject.get("access_token"));
            oAuthDto.setRefreshToken((String) jsonObject.get("refresh_token"));
            oAuthDto.setExpireTime((Long) jsonObject.get("expires_in"));
            String scope = (String) jsonObject.get("scope");

            log.info("access token : " + oAuthDto.getAccessToken());
            log.info("refresh token : " + oAuthDto.getRefreshToken());
            log.info("expires_in : " + oAuthDto.getExpireTime());
            log.info("scope : " + scope);
        }
        catch (ParseException e) {
            log.error("fail parsing token info");
            e.printStackTrace();
        }

        return oAuthDto;
    }

    public static String parseGoogleEmailInfo(String emailInfoFromGoogle) {
        JSONParser jsonParser = new JSONParser();

        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(emailInfoFromGoogle);
            String email = (String) jsonObject.get("email");
            log.info("email : " + email);
            return email;
        }
        catch (ParseException e) {
            log.error("fail parsing email info");
            e.printStackTrace();
        }

        return null;
    }
}
