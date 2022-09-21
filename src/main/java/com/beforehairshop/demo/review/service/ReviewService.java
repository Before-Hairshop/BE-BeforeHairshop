package com.beforehairshop.demo.review.service;

import com.beforehairshop.demo.aws.S3Uploader;
import com.beforehairshop.demo.aws.handler.CloudFrontUrlHandler;
import com.beforehairshop.demo.aws.service.AmazonS3Service;
import com.beforehairshop.demo.constant.StatusKind;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfileDesiredHairstyleImage;
import com.beforehairshop.demo.member.repository.MemberRepository;
import com.beforehairshop.demo.response.ResultDto;
import com.beforehairshop.demo.review.domain.Review;
import com.beforehairshop.demo.review.domain.ReviewHashtag;
import com.beforehairshop.demo.review.domain.ReviewImage;
import com.beforehairshop.demo.review.dto.patch.ReviewPatchRequestDto;
import com.beforehairshop.demo.review.dto.save.ReviewSaveRequestDto;
import com.beforehairshop.demo.review.repository.ReviewHashtagRepository;
import com.beforehairshop.demo.review.repository.ReviewImageRepository;
import com.beforehairshop.demo.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.beforehairshop.demo.response.ResultDto.*;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewHashtagRepository reviewHashtagRepository;
    private final ReviewImageRepository reviewImageRepository;


    @Transactional
    public ResponseEntity<ResultDto> save(Member member, ReviewSaveRequestDto reviewSaveRequestDto) {
        Member reviewer = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);
        Member hairDesigner = memberRepository.findByIdAndStatus(reviewSaveRequestDto.getHairDesignerId(), StatusKind.NORMAL.getId()).orElse(null);

        if (reviewer == null || hairDesigner == null || hairDesigner.getDesignerFlag() != 1)
            return makeResult(HttpStatus.INTERNAL_SERVER_ERROR, "리뷰 대상이 유효하지 않거나, 헤어 디자이너가 아니다.");

        Review review = reviewRepository.save(
                reviewSaveRequestDto.toEntity(member, hairDesigner)
        );

        reviewHashtagRepository.saveAll(reviewSaveRequestDto.getHashtagList()
                .stream()
                .map(reviewHashtagSaveDto -> reviewHashtagSaveDto.toEntity(review))
                .collect(Collectors.toList()));

        return makeResult(HttpStatus.OK, review);
    }

    @Transactional
    public ResponseEntity<ResultDto> findManyByHairDesigner(BigInteger hairDesignerId, Pageable pageable) {
        List<Review> reviewList = reviewRepository.findAllByHairDesignerIdAndStatus(hairDesignerId, StatusKind.NORMAL.getId(), pageable);

        return makeResult(HttpStatus.OK, reviewList);
    }

    @Transactional
    public ResponseEntity<ResultDto> patchOne(BigInteger reviewId, ReviewPatchRequestDto reviewPatchRequestDto) {
        Review review = reviewRepository.findByIdAndStatus(reviewId, StatusKind.NORMAL.getId()).orElse(null);
        if (review == null)
            return makeResult(HttpStatus.BAD_REQUEST, "해당 id를 가지는 리뷰는 없습니다.");

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
            List<ReviewHashtag> reviewHashtagList = reviewHashtagRepository.findAllByReviewAndStatus(review, StatusKind.NORMAL.getId());
            reviewHashtagRepository.deleteAllInBatch(reviewHashtagList);

            // review hash tag 생성
            reviewHashtagRepository.saveAll(reviewPatchRequestDto.getHashtagList()
                    .stream()
                    .map(reviewHashtagPatchRequestDto -> reviewHashtagPatchRequestDto.toEntity(review))
                    .collect(Collectors.toList()));
        }

        return makeResult(HttpStatus.OK, review);
    }

    @Transactional
    public ResponseEntity<ResultDto> saveImage(Member reviewer, BigInteger reviewId
            , Integer reviewImageCount, AmazonS3Service amazonS3Service) {
        Review review = reviewRepository.findByIdAndStatus(reviewId, StatusKind.NORMAL.getId()).orElse(null);
        if (!review.getReviewer().getId().equals(reviewer.getId()))
            return makeResult(HttpStatus.BAD_REQUEST, "수정 권한이 없는 유저입니다.");

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
                    CloudFrontUrlHandler.getReviewImageS3Path(review.getId(), reviewImage.getId())
            );

            reviewImagePreSignedList.add(preSignedUrl);

            // image url 수정
            reviewImage.setImageUrl(
                    CloudFrontUrlHandler.getReviewImageUrl(review.getId(), reviewImage.getId())
            );

        }

        return makeResult(HttpStatus.OK, reviewImagePreSignedList);
    }

    @Transactional
    public ResponseEntity<ResultDto> patchImage(Member member, BigInteger reviewId
            , String[] deleteImageUrlList, Integer reviewImageCount, AmazonS3Service amazonS3Service) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (!review.getReviewer().getId().equals(member.getId())) {
            return makeResult(HttpStatus.BAD_REQUEST, "수정 권한이 없는 유저입니다.");
        }

        // 삭제할 이미지 삭제
        for (int i = 0; i < deleteImageUrlList.length; i++) {
            ReviewImage reviewImage
                    = reviewImageRepository.findByImageUrlAndStatus(deleteImageUrlList[i], StatusKind.NORMAL.getId()).orElse(null);

            if (reviewImage == null)
                return makeResult(HttpStatus.BAD_REQUEST, "존재하지 않는 image 이다");

            reviewImageRepository.delete(reviewImage);
        }

        // 프론트엔드에서 요청한 이미지의 개수만큼 presigned url 을 만들어 리턴한다.
        List<String> reviewImagePreSignedUrlList = new ArrayList<>();
        for (int i = 0; i < reviewImageCount; i++) {
            ReviewImage reviewImage = ReviewImage.builder()
                    .review(review)
                    .imageUrl(null)
                    .status(StatusKind.NORMAL.getId())
                    .build();

            reviewImage = reviewImageRepository.save(reviewImage);
            String preSignedUrl = amazonS3Service.generatePreSignedUrl(
                    CloudFrontUrlHandler.getReviewImageS3Path(review.getId(), reviewImage.getId())
            );

            reviewImagePreSignedUrlList.add(preSignedUrl);

            // image url 수정
            reviewImage.setImageUrl(CloudFrontUrlHandler.getReviewImageUrl(review.getId(), reviewImage.getId()));

        }

        return makeResult(HttpStatus.OK, reviewImagePreSignedUrlList);
    }
}
