package com.beforehairshop.demo.member.handler;

import java.math.BigInteger;

public class CloudFrontUrlHandler {
    public static final String CLOUD_FRONT_DOMAIN_NAME = "https://d8ov181gler1c.cloudfront.net";

    public static String getProfileOfUserS3Path(BigInteger memberId, String type) {
        if (type.equals("front")) {
            return "profile/" + memberId + "/front_image.jpg";
        } else if (type.equals("side")) {
            return "profile/" + memberId + "/side_image.jpg";
        } else {    // back
            return "profile/" + memberId + "/back_image.jpg";
        }
    }

    public static String getProfileOfUserImageUrl(BigInteger memberId, String type) {

        if (type.equals("front")) {
            return CLOUD_FRONT_DOMAIN_NAME + "/profile/" + memberId + "/front_image.jpg";
        } else if (type.equals("side")) {
            return CLOUD_FRONT_DOMAIN_NAME + "/profile/" + memberId + "/side_image.jpg";
        } else {    // back
            return CLOUD_FRONT_DOMAIN_NAME + "/profile/" + memberId + "/back_image.jpg";
        }

    }
}
