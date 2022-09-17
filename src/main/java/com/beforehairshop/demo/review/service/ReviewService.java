package com.beforehairshop.demo.review.service;

import com.beforehairshop.demo.aws.S3Uploader;
import com.beforehairshop.demo.constant.StatusKind;
import com.beforehairshop.demo.member.domain.Member;
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
    private final S3Uploader s3Uploader;

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

//    @Transactional
//    public ResponseEntity<ResultDto> saveImage(Member reviewer, ReviewImageSaveRequestDto reviewImageSaveRequestDto) throws IOException {
//        System.out.println(reviewImageSaveRequestDto.getReviewId());
//        Review review = reviewRepository.findById(reviewImageSaveRequestDto.getReviewId()).orElse(null);
//
//        List<ReviewImage> reviewImageList = new ArrayList<>();
//
//        for (ImageFile image : reviewImageSaveRequestDto.getImageList()) {
//            ReviewImage reviewImage = ReviewImage.builder().imageUrl(null).review(review).build();
//            reviewImage = reviewImageRepository.save(reviewImage);
//            reviewImage = reviewImageRepository.findById(reviewImage.getId()).orElse(null);
//
//            String imageUrl = s3Uploader.upload(image.getImage(), "review/" + reviewImage.getId() + ".jpg");
//            reviewImage.setImageUrl(imageUrl);
//            reviewImage.setStatus(1);
//
//            reviewImageList.add(reviewImage);
//        }
//
//        return makeResult(HttpStatus.OK, reviewImageList);
//    }

    @Transactional
    public ResponseEntity<ResultDto> saveImage(Member reviewer, BigInteger reviewId, MultipartFile[] files) throws IOException {
        Review review = reviewRepository.findByIdAndStatus(reviewId, StatusKind.NORMAL.getId()).orElse(null);

        List<ReviewImage> reviewImageList = new ArrayList<>();

        for (MultipartFile image : files) {
            ReviewImage reviewImage = ReviewImage.builder().imageUrl(null).review(review).build();
            reviewImage = reviewImageRepository.save(reviewImage);
            reviewImage = reviewImageRepository.findByIdAndStatus(reviewImage.getId(), StatusKind.NORMAL.getId()).orElse(null);

            String imageUrl = s3Uploader.upload(image, "review/" + reviewId + "/" + reviewImage.getId() + ".jpg");
            reviewImage.setImageUrl(imageUrl);
            reviewImage.setStatus(1);

            reviewImageList.add(reviewImage);
        }

        return makeResult(HttpStatus.OK, reviewImageList);
    }
    @Transactional
    public ResponseEntity<ResultDto> addImage(BigInteger reviewId, MultipartFile[] addImages) throws IOException {
        Review review = reviewRepository.findByIdAndStatus(reviewId, StatusKind.NORMAL.getId()).orElse(null);

        List<ReviewImage> reviewImageList = new ArrayList<>();
        for(MultipartFile image : addImages) {
            ReviewImage reviewImage = ReviewImage.builder().imageUrl(null).review(review).build();
            reviewImage = reviewImageRepository.save(reviewImage);
            reviewImage = reviewImageRepository.findByIdAndStatus(reviewImage.getId(), StatusKind.NORMAL.getId()).orElse(null);

            String imageUrl = s3Uploader.upload(image, "review/" + review.getId() + "/" + reviewImage.getId() + ".jpg");
            reviewImage.setImageUrl(imageUrl);
            reviewImage.setStatus(1);

            reviewImageList.add(reviewImage);
        }

        return makeResult(HttpStatus.OK, reviewImageList);
    }

    @Transactional
    public ResponseEntity<ResultDto> removeImage(BigInteger reviewId, List<BigInteger> deleteReviewImageIdList) {

        for (BigInteger deleteImageId : deleteReviewImageIdList) {
            ReviewImage reviewImage = reviewImageRepository.findByIdAndStatus(deleteImageId, StatusKind.NORMAL.getId()).orElse(null);
            if (!reviewImage.getReview().getId().equals(reviewId)) {
                return makeResult(HttpStatus.BAD_REQUEST, "해당 리뷰에 대한 이미지를 삭제하는 요청이 아닙니다. 삭제 요청하는 이미지의 ID를 확인해주세요");
            }
            reviewImageRepository.delete(reviewImage);
        }

        return makeResult(HttpStatus.OK, "이미지 삭제 완료");
    }
}
