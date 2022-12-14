package com.beforehairshop.demo.review.service;

import com.beforehairshop.demo.aws.handler.CloudFrontUrlHandler;
import com.beforehairshop.demo.aws.service.AmazonS3Service;
import com.beforehairshop.demo.constant.member.StatusKind;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.hairdesigner.repository.HairDesignerProfileRepository;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.repository.MemberRepository;
import com.beforehairshop.demo.response.ResultDto;
import com.beforehairshop.demo.review.domain.Review;
import com.beforehairshop.demo.review.domain.ReviewHashtag;
import com.beforehairshop.demo.review.domain.ReviewImage;
import com.beforehairshop.demo.review.dto.ReviewDto;
import com.beforehairshop.demo.review.dto.ReviewHashtagDto;
import com.beforehairshop.demo.review.dto.ReviewImageDto;
import com.beforehairshop.demo.review.dto.patch.ReviewHashtagPatchRequestDto;
import com.beforehairshop.demo.review.dto.patch.ReviewImagePatchRequestDto;
import com.beforehairshop.demo.review.dto.patch.ReviewPatchRequestDto;
import com.beforehairshop.demo.review.dto.response.ReviewDetailResponseDto;
import com.beforehairshop.demo.review.dto.save.ReviewHashtagSaveRequestDto;
import com.beforehairshop.demo.review.dto.save.ReviewSaveRequestDto;
import com.beforehairshop.demo.review.repository.ReviewHashtagRepository;
import com.beforehairshop.demo.review.repository.ReviewImageRepository;
import com.beforehairshop.demo.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.beforehairshop.demo.response.ResultDto.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {
    private final CloudFrontUrlHandler cloudFrontUrlHandler;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final HairDesignerProfileRepository hairDesignerProfileRepository;
    private final ReviewHashtagRepository reviewHashtagRepository;
    private final ReviewImageRepository reviewImageRepository;


    @Transactional
    public ResponseEntity<ResultDto> save(Member member, ReviewSaveRequestDto reviewSaveRequestDto) {
        if (member == null) {
            log.error("[POST] /api/v1/reviews - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        if (!saveDtoIsValid(reviewSaveRequestDto)) {
            log.error("[POST] /api/v1/reviews - 204 (?????? ????????? ????????? ?????? ??????)");
            return makeResult(HttpStatus.NO_CONTENT, "?????? ????????? ????????? ????????? ???????????? ???????????????.");
        }
        Member reviewer = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);
        Member hairDesigner = memberRepository.findByIdAndStatus(reviewSaveRequestDto.getHairDesignerId(), StatusKind.NORMAL.getId()).orElse(null);
        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(
                hairDesigner, StatusKind.NORMAL.getId()
        ).orElse(null);

        if (reviewer == null || hairDesigner == null || hairDesigner.getDesignerFlag() != 1
                || hairDesignerProfile == null) {
            log.error("[POST] /api/v1/reviews - 404 (?????? ????????? ???????????? ?????????, ??????????????? ?????????)");
            return makeResult(HttpStatus.NOT_FOUND, "?????? ????????? ???????????? ?????????, ?????? ??????????????? ?????????.");
        }
        Review review = reviewRepository.save(new Review(reviewSaveRequestDto, reviewer, hairDesignerProfile, StatusKind.NORMAL.getId()));

        for (ReviewHashtagSaveRequestDto saveRequestDto : reviewSaveRequestDto.getHashtagList()) {
            ReviewHashtag reviewHashtag = new ReviewHashtag(saveRequestDto.getHashtag(), StatusKind.NORMAL.getId());
            review.addReviewHashtag(reviewHashtag);
        }

//        reviewSaveRequestDto.getHashtagList().stream()
//                        .map(reviewHashtagSaveRequestDto -> review.getReviewHashtagSet().add(
//                                reviewHashtagSaveRequestDto.toEntity(review)
//                        ));



//        reviewHashtagRepository.saveAll(reviewSaveRequestDto.getHashtagList()
//                .stream()
//                .map(reviewHashtagSaveDto -> reviewHashtagSaveDto.toEntity(review))
//                .collect(Collectors.toList()));

        return makeResult(HttpStatus.OK, new ReviewDto(review));
    }

    private boolean saveDtoIsValid(ReviewSaveRequestDto reviewSaveRequestDto) {
        return reviewSaveRequestDto.getHairDesignerId() != null && reviewSaveRequestDto.getTotalRating() != null
                && reviewSaveRequestDto.getStyleRating() != null && reviewSaveRequestDto.getServiceRating() != null
                && reviewSaveRequestDto.getContent() != null;
    }

    @Transactional
    public ResponseEntity<ResultDto> findManyByHairDesigner(Member member, BigInteger hairDesignerId, Pageable pageable) {
        if (member == null) {
            log.error("[GET] /api/v1/reviews/list - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        List<Review> reviewList = reviewRepository.findAllByHairDesignerProfileIdAndStatus(
                hairDesignerId
                , StatusKind.NORMAL.getId()
                , pageable
        );

        List<ReviewDetailResponseDto> reviewDetailResponseDtoList = new ArrayList<>();
        for (Review review : reviewList) {
            List<ReviewHashtagDto> hashtagDtoList = reviewHashtagRepository.findByReviewAndStatus(review, StatusKind.NORMAL.getId())
                    .stream()
                    .map(ReviewHashtagDto::new)
                    .collect(Collectors.toList());

            List<ReviewImageDto> imageDtoList = reviewImageRepository.findByReviewAndStatus(review, StatusKind.NORMAL.getId())
                    .stream()
                    .map(ReviewImageDto::new)
                    .collect(Collectors.toList());

            //Member reviewer = memberRepository.findByIdAndStatus(review.getReviewer().getId(), st)
            if (review.getReviewer().getName() == null)
                continue;

            reviewDetailResponseDtoList.add(new ReviewDetailResponseDto(review.getReviewer().getName()
                    , new ReviewDto(review)
                    , hashtagDtoList
                    , imageDtoList));
        }

        return makeResult(HttpStatus.OK, reviewDetailResponseDtoList);
    }

    @Transactional
    public ResponseEntity<ResultDto> patchOne(Member member, BigInteger reviewId, ReviewPatchRequestDto reviewPatchRequestDto) {
        if (member == null) {
            log.error("[PATCH] /api/v1/reviews - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        Review review = reviewRepository.findByIdAndStatus(reviewId, StatusKind.NORMAL.getId()).orElse(null);
        if (review == null) {
            log.error("[PATCH] /api/v1/reviews - 400 (????????? ?????? ID)");
            return makeResult(HttpStatus.BAD_REQUEST, "?????? id??? ????????? ????????? ????????????.");
        }

        if (!review.getReviewer().getId().equals(member.getId())) {
            log.error("[PATCH] /api/v1/reviews - 503 (?????? ????????? ????????? ?????? ??????)");
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "?????? ????????? ????????? ????????? ?????? ???????????????.");
        }


        if (reviewPatchRequestDto.getTotalRating() != null)
            review.setTotalRating(reviewPatchRequestDto.getTotalRating());

        if (reviewPatchRequestDto.getStyleRating() != null)
            review.setStyleRating(reviewPatchRequestDto.getStyleRating());

        if (reviewPatchRequestDto.getServiceRating() != null)
            review.setServiceRating(reviewPatchRequestDto.getServiceRating());

        if (reviewPatchRequestDto.getContent() != null)
            review.setContent(reviewPatchRequestDto.getContent());

        if (reviewPatchRequestDto.getHashtagList() != null) {
            // review hash tag ??????
            review.getReviewHashtagSet().clear();

//            List<ReviewHashtag> reviewHashtagList = reviewHashtagRepository.findByReviewAndStatus(review, StatusKind.NORMAL.getId());
//            reviewHashtagRepository.deleteAllInBatch(reviewHashtagList);

            // review hash tag ??????
            for (ReviewHashtagPatchRequestDto patchRequestDto : reviewPatchRequestDto.getHashtagList()) {
                ReviewHashtag reviewHashtag = new ReviewHashtag(patchRequestDto.getHashtag(), StatusKind.NORMAL.getId());

                review.addReviewHashtag(reviewHashtag);
            }

//            reviewHashtagRepository.saveAll(reviewPatchRequestDto.getHashtagList()
//                    .stream()
//                    .map(reviewHashtagPatchRequestDto -> reviewHashtagPatchRequestDto.toEntity(review))
//                    .collect(Collectors.toList()));
        }

        return makeResult(HttpStatus.OK, new ReviewDto(review));
    }

    @Transactional
    public ResponseEntity<ResultDto> saveImage(Member reviewer, BigInteger reviewId
            , Integer reviewImageCount, AmazonS3Service amazonS3Service) {

        if (reviewer == null) {
            log.error("[POST] /api/v1/reviews/image - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        Review review = reviewRepository.findByIdAndStatus(reviewId, StatusKind.NORMAL.getId()).orElse(null);
        if (review == null) {
            log.error("[POST] /api/v1/reviews/image - 404 (????????? ?????? ID)");
            return makeResult(HttpStatus.NOT_FOUND, "????????? ?????? ID ?????????.");
        }
        if (!review.getReviewer().getId().equals(reviewer.getId())) {
            log.error("[POST] /api/v1/reviews/image - 503 (?????? ?????? ?????? ??????)");
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "?????? ????????? ?????? ???????????????.");
        }
        List<String> reviewImagePreSignedList = new ArrayList<>();
        for (int i = 0; i < reviewImageCount; i++) {
            ReviewImage reviewImage
                    = ReviewImage.builder()
                    .review(review)
                    .imageUrl(null)
                    .status(StatusKind.NORMAL.getId())
                    .build();

            reviewImage = reviewImageRepository.save(reviewImage);
            String preSignedUrl = amazonS3Service.generatePreSignedUrl(
                    cloudFrontUrlHandler.getReviewImageS3Path(review.getId(), reviewImage.getId())
            );

            reviewImagePreSignedList.add(preSignedUrl);

            // image url ??????
            reviewImage.setImageUrl(
                    cloudFrontUrlHandler.getReviewImageUrl(review.getId(), reviewImage.getId())
            );

            review.getReviewImageSet().add(reviewImage);
        }

        return makeResult(HttpStatus.OK, reviewImagePreSignedList);
    }

    @Transactional
    public ResponseEntity<ResultDto> patchImage(Member member, ReviewImagePatchRequestDto imagePatchRequestDto
            , AmazonS3Service amazonS3Service) {
        if (member == null) {
            log.error("[PATCH] /api/v1/reviews/image - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        Review review = reviewRepository.findById(imagePatchRequestDto.getReviewId()).orElse(null);
        if (review == null) {
            log.error("[PATCH] /api/v1/reviews/image - 404 (????????? ?????? ID)");
            return makeResult(HttpStatus.NOT_FOUND, "????????? ?????? ID ??? ?????????????????????");
        }
        if (!review.getReviewer().getId().equals(member.getId())) {
            log.error("[PATCH] /api/v1/reviews/image - 503 (?????? ?????? ?????? ??????)");
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "?????? ????????? ?????? ???????????????.");
        }

        // ????????? ????????? ??????
        if (imagePatchRequestDto.getDeleteImageUrlList() != null) {
            List<ReviewImage> reviewImageList = new ArrayList<>();
            for (String imageUrl : imagePatchRequestDto.getDeleteImageUrlList()) {
                ReviewImage reviewImage
                        = reviewImageRepository.findByImageUrlAndStatus(imageUrl, StatusKind.NORMAL.getId()).orElse(null);

                if (reviewImage == null) {
                    log.error("[PATCH] /api/v1/reviews/image - 400 (???????????? ?????? ????????? URL ??????. ????????????)");
                    return makeResult(HttpStatus.BAD_REQUEST, "???????????? ?????? image ??????");
                }
                reviewImageList.add(reviewImage);
//            review.getReviewImageSet().remove(reviewImage);
//            reviewImageRepository.delete(reviewImage);
            }

            for (ReviewImage reviewImage : reviewImageList)
                review.getReviewImageSet().remove(reviewImage);
        }

        // ????????????????????? ????????? ???????????? ???????????? presigned url ??? ????????? ????????????.
        List<String> reviewImagePreSignedUrlList = new ArrayList<>();
        for (int i = 0; i < imagePatchRequestDto.getAddReviewImageCount(); i++) {
            ReviewImage reviewImage = ReviewImage.builder()
                    .review(review)
                    .imageUrl(null)
                    .status(StatusKind.NORMAL.getId())
                    .build();

            reviewImage = reviewImageRepository.save(reviewImage);
            String preSignedUrl = amazonS3Service.generatePreSignedUrl(
                    cloudFrontUrlHandler.getReviewImageS3Path(review.getId(), reviewImage.getId())
            );

            reviewImagePreSignedUrlList.add(preSignedUrl);

            // image url ??????
            reviewImage.setImageUrl(cloudFrontUrlHandler.getReviewImageUrl(review.getId(), reviewImage.getId()));

            review.getReviewImageSet().add(reviewImage);
        }

        return makeResult(HttpStatus.OK, reviewImagePreSignedUrlList);
    }

    @Transactional
    public ResponseEntity<ResultDto> delete(Member member, BigInteger reviewId) {
        if (member == null) {
            log.error("[DEL] /api/v1/reviews - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        Review review = reviewRepository.findByIdAndStatus(reviewId, StatusKind.NORMAL.getId()).orElse(null);
        if (review == null) {
            log.error("[DEL] /api/v1/reviews - 404 (????????? ?????? ID)");
            return makeResult(HttpStatus.NOT_FOUND, "?????? ID??? ????????? ????????? ???????????? ????????????.");
        }
        if (!review.getReviewer().getId().equals(member.getId())) {
            log.error("[DEL] /api/v1/reviews - 503 (????????? ????????? ?????? ??????)");
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "????????? ????????? ?????? ???????????????.");
        }

        reviewRepository.delete(review);

        return makeResult(HttpStatus.OK, "?????? ??????");
    }
}
