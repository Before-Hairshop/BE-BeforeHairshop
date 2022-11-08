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
            log.error("[POST] /api/v1/reviews - 504 (세션 만료)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }
        if (!saveDtoIsValid(reviewSaveRequestDto)) {
            log.error("[POST] /api/v1/reviews - 204 (리뷰 저장에 필요한 정보 부족)");
            return makeResult(HttpStatus.NO_CONTENT, "리뷰 저장에 필요한 정보가 입력되지 않았습니다.");
        }
        Member reviewer = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);
        Member hairDesigner = memberRepository.findByIdAndStatus(reviewSaveRequestDto.getHairDesignerId(), StatusKind.NORMAL.getId()).orElse(null);
        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(
                hairDesigner, StatusKind.NORMAL.getId()
        ).orElse(null);

        if (reviewer == null || hairDesigner == null || hairDesigner.getDesignerFlag() != 1
                || hairDesignerProfile == null) {
            log.error("[POST] /api/v1/reviews - 404 (리뷰 대상이 유효하지 않거나, 디자이너가 아니다)");
            return makeResult(HttpStatus.NOT_FOUND, "리뷰 대상이 유효하지 않거나, 헤어 디자이너가 아니다.");
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
            log.error("[GET] /api/v1/reviews/list - 504 (세션 만료)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
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
            log.error("[PATCH] /api/v1/reviews - 504 (세션 만료)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }
        Review review = reviewRepository.findByIdAndStatus(reviewId, StatusKind.NORMAL.getId()).orElse(null);
        if (review == null) {
            log.error("[PATCH] /api/v1/reviews - 400 (잘못된 리뷰 ID)");
            return makeResult(HttpStatus.BAD_REQUEST, "해당 id를 가지는 리뷰는 없습니다.");
        }

        if (!review.getReviewer().getId().equals(member.getId())) {
            log.error("[PATCH] /api/v1/reviews - 503 (리뷰 수정할 권한이 없는 유저)");
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "해당 리뷰를 수정할 권한이 없는 유저입니다.");
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
            // review hash tag 삭제
            review.getReviewHashtagSet().clear();

//            List<ReviewHashtag> reviewHashtagList = reviewHashtagRepository.findByReviewAndStatus(review, StatusKind.NORMAL.getId());
//            reviewHashtagRepository.deleteAllInBatch(reviewHashtagList);

            // review hash tag 생성
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
            log.error("[POST] /api/v1/reviews/image - 504 (세션 만료)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }
        Review review = reviewRepository.findByIdAndStatus(reviewId, StatusKind.NORMAL.getId()).orElse(null);
        if (review == null) {
            log.error("[POST] /api/v1/reviews/image - 404 (잘못된 리뷰 ID)");
            return makeResult(HttpStatus.NOT_FOUND, "잘못된 리뷰 ID 입니다.");
        }
        if (!review.getReviewer().getId().equals(reviewer.getId())) {
            log.error("[POST] /api/v1/reviews/image - 503 (수정 권한 없는 유저)");
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "수정 권한이 없는 유저입니다.");
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

            // image url 수정
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
            log.error("[PATCH] /api/v1/reviews/image - 504 (세션 만료)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }
        Review review = reviewRepository.findById(imagePatchRequestDto.getReviewId()).orElse(null);
        if (review == null) {
            log.error("[PATCH] /api/v1/reviews/image - 404 (잘못된 리뷰 ID)");
            return makeResult(HttpStatus.NOT_FOUND, "잘못된 리뷰 ID 가 입력되었습니다");
        }
        if (!review.getReviewer().getId().equals(member.getId())) {
            log.error("[PATCH] /api/v1/reviews/image - 503 (수정 권한 없는 유저)");
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "수정 권한이 없는 유저입니다.");
        }

        // 삭제할 이미지 삭제
        if (imagePatchRequestDto.getDeleteImageUrlList() != null) {
            List<ReviewImage> reviewImageList = new ArrayList<>();
            for (String imageUrl : imagePatchRequestDto.getDeleteImageUrlList()) {
                ReviewImage reviewImage
                        = reviewImageRepository.findByImageUrlAndStatus(imageUrl, StatusKind.NORMAL.getId()).orElse(null);

                if (reviewImage == null) {
                    log.error("[PATCH] /api/v1/reviews/image - 400 (존재하지 않는 이미지 URL 이다. 삭제불가)");
                    return makeResult(HttpStatus.BAD_REQUEST, "존재하지 않는 image 이다");
                }
                reviewImageList.add(reviewImage);
//            review.getReviewImageSet().remove(reviewImage);
//            reviewImageRepository.delete(reviewImage);
            }

            for (ReviewImage reviewImage : reviewImageList)
                review.getReviewImageSet().remove(reviewImage);
        }

        // 프론트엔드에서 요청한 이미지의 개수만큼 presigned url 을 만들어 리턴한다.
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

            // image url 수정
            reviewImage.setImageUrl(cloudFrontUrlHandler.getReviewImageUrl(review.getId(), reviewImage.getId()));

            review.getReviewImageSet().add(reviewImage);
        }

        return makeResult(HttpStatus.OK, reviewImagePreSignedUrlList);
    }

    @Transactional
    public ResponseEntity<ResultDto> delete(Member member, BigInteger reviewId) {
        if (member == null) {
            log.error("[DEL] /api/v1/reviews - 504 (세션 만료)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }
        Review review = reviewRepository.findByIdAndStatus(reviewId, StatusKind.NORMAL.getId()).orElse(null);
        if (review == null) {
            log.error("[DEL] /api/v1/reviews - 404 (잘못된 리뷰 ID)");
            return makeResult(HttpStatus.NOT_FOUND, "해당 ID를 가지는 리뷰는 존재하지 않습니다.");
        }
        if (!review.getReviewer().getId().equals(member.getId())) {
            log.error("[DEL] /api/v1/reviews - 503 (삭제할 권한이 없는 유저)");
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "삭제할 권한이 없는 유저입니다.");
        }

        reviewRepository.delete(review);

        return makeResult(HttpStatus.OK, "삭제 완료");
    }
}
