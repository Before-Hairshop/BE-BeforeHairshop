package com.beforehairshop.demo.aws.handler;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
public class CloudFrontUrlHandler {

    private final String CLOUD_FRONT_DOMAIN_NAME;

    @Autowired
    public CloudFrontUrlHandler(@Value("${cloud.aws.cloudfront.domain}") String CLOUD_FRONT_DOMAIN_NAME) {
        this.CLOUD_FRONT_DOMAIN_NAME = CLOUD_FRONT_DOMAIN_NAME;
    }


    /**
     * <h2>유저 프로필의 정면, 측면, 후면 사진 관련 경로</h2>
     */
    public String getProfileOfUserS3Path(BigInteger memberId, String type) {
        if (type.equals("front")) {
            return "profile/" + memberId + "/front_image.jpg";
        } else if (type.equals("side")) {
            return "profile/" + memberId + "/side_image.jpg";
        } else {    // back
            return "profile/" + memberId + "/back_image.jpg";
        }
    }

    public String getProfileOfUserImageUrl(BigInteger memberId, String type) {

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
    public String getProfileOfDesignerS3Path(BigInteger memberId) {
        return "designer_profile/" + memberId + "/profile.jpg";
    }

    public String getProfileOfDesignerImageUrl(BigInteger memberId) {
        return CLOUD_FRONT_DOMAIN_NAME + "/designer_profile/" + memberId + "/profile.jpg";
    }


    /**
     * <h2>유저 프로필의 원하는 스타일 사진들에 대한 관련 경로</h2>
     */
    public String getProfileOfUserDesiredStyleS3Path(BigInteger profileId, BigInteger imageId) {
        return "hairstyle_wishlist/" + profileId + "/" + imageId + ".jpg";
    }

    public String getProfileOfUserDesiredStyleImageUrl(BigInteger profileId, BigInteger imageId) {
        return CLOUD_FRONT_DOMAIN_NAME + "/hairstyle_wishlist/" + profileId + "/" + imageId + ".jpg";
    }


    /**
     * <h2>리뷰 이미지 관련 경로</h2>
     */
    public String getReviewImageS3Path(BigInteger reviewId, BigInteger reviewImageId) {
        return "review/" + reviewId + "/" + reviewImageId + ".jpg";
    }

    public String getReviewImageUrl(BigInteger reviewId, BigInteger reviewImageId) {
        return CLOUD_FRONT_DOMAIN_NAME + "/review/" + reviewId + "/" + reviewImageId + ".jpg";
    }

    /**
     *  <h2>스타일 추천서 이미지 관련 경로</h2>
     */
    public String getRecommendImageS3Path(BigInteger styleRecommendId, BigInteger styleRecommendImageId) {
        return "recommend/" + styleRecommendId + "/" + styleRecommendImageId + ".png";
    }

    public String getRecommendImageUrl(BigInteger styleRecommendId, BigInteger styleRecommendImageId) {
        return CLOUD_FRONT_DOMAIN_NAME + "/recommend/" + styleRecommendId + "/" + styleRecommendImageId + ".png";
    }

    public String getLogoUrl() {
        return CLOUD_FRONT_DOMAIN_NAME + "/logo.png";
    }
}
