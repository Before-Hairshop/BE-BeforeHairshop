package com.beforehairshop.demo.aws.handler;

import java.math.BigInteger;

public class CloudFrontUrlHandler {
    public static final String CLOUD_FRONT_DOMAIN_NAME = "https://d8ov181gler1c.cloudfront.net";

    /**
     * <h2>유저 프로필의 정면, 측면, 후면 사진 관련 경로</h2>
     */
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

    /**
     * <h2>디자이너 프로필의 정면 사진 관련 경로</h2>
     */
    public static String getProfileOfDesignerS3Path(BigInteger memberId) {
        return "designer_profile/" + memberId + "/profile.jpg";
    }

    public static String getProfileOfDesignerImageUrl(BigInteger memberId) {
        return CLOUD_FRONT_DOMAIN_NAME + "/designer_profile/" + memberId + "/profile.jpg";
    }


    /**
     * <h2>유저 프로필의 원하는 스타일 사진들에 대한 관련 경로</h2>
     */
    public static String getProfileOfUserDesiredStyleS3Path(BigInteger profileId, BigInteger imageId) {
        return "hairstyle_wishlist/" + profileId + "/" + imageId + ".jpg";
    }

    public static String getProfileOfUserDesiredStyleImageUrl(BigInteger profileId, BigInteger imageId) {
        return CLOUD_FRONT_DOMAIN_NAME + "/hairstyle_wishlist/" + profileId + "/" + imageId + ".jpg";
    }


    /**
     * <h2>리뷰 이미지 관련 경로</h2>
     */
    public static String getReviewImageS3Path(BigInteger reviewId, BigInteger reviewImageId) {
        return "review/" + reviewId + "/" + reviewImageId + ".jpg";
    }

    public static String getReviewImageUrl(BigInteger reviewId, BigInteger reviewImageId) {
        return CLOUD_FRONT_DOMAIN_NAME + "/review/" + reviewId + "/" + reviewImageId + ".jpg";
    }

    /**
     *  <h2>스타일 추천서 이미지 관련 경로</h2>
     */
    public static String getRecommendImageS3Path(BigInteger styleRecommendId, BigInteger styleRecommendImageId) {
        return "recommend/" + styleRecommendId + "/" + styleRecommendImageId + ".png";
    }

    public static String getRecommendImageUrl(BigInteger styleRecommendId, BigInteger styleRecommendImageId) {
        return CLOUD_FRONT_DOMAIN_NAME + "/recommend/" + styleRecommendId + "/" + styleRecommendImageId + ".png";
    }
}
