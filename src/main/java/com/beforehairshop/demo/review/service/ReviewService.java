package com.beforehairshop.demo.review.service;

import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.repository.MemberRepository;
import com.beforehairshop.demo.response.ResultDto;
import com.beforehairshop.demo.review.domain.Review;
import com.beforehairshop.demo.review.domain.ReviewHashtag;
import com.beforehairshop.demo.review.dto.ReviewHashtagSaveRequestDto;
import com.beforehairshop.demo.review.dto.ReviewPatchRequestDto;
import com.beforehairshop.demo.review.dto.ReviewSaveRequestDto;
import com.beforehairshop.demo.review.repository.ReviewHashtagRepository;
import com.beforehairshop.demo.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import static com.beforehairshop.demo.response.ResultDto.*;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewHashtagRepository reviewHashtagRepository;

    @Transactional
    public ResponseEntity<ResultDto> save(ReviewSaveRequestDto reviewSaveRequestDto) {
        Member member = memberRepository.findById(reviewSaveRequestDto.getMemberId()).orElse(null);
        Member hairDesigner = memberRepository.findById(reviewSaveRequestDto.getHairDesignerId()).orElse(null);

        if (member != null && hairDesigner != null && hairDesigner.getDesignerFlag() != 1)
            return makeResult(HttpStatus.INTERNAL_SERVER_ERROR, "리뷰 대상이 없거나, 헤어 디자이너가 아니다.");

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
        List<Review> reviewList = reviewRepository.findAllByHairDesignerId(hairDesignerId, pageable);

        return makeResult(HttpStatus.OK, reviewList);
    }

    @Transactional
    public ResponseEntity<ResultDto> patchOne(BigInteger reviewId, ReviewPatchRequestDto reviewPatchRequestDto) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null)
            return makeResult(HttpStatus.BAD_REQUEST, "해당 id를 가지는 리뷰는 없습니다.");

        review.patchReview(reviewPatchRequestDto);

        // review hash tag 삭제
        List<ReviewHashtag> reviewHashtagList = reviewHashtagRepository.findAllByReview(review);
        reviewHashtagRepository.deleteAllInBatch(reviewHashtagList);

        // review hash tag 생성
        reviewHashtagRepository.saveAll(reviewPatchRequestDto.getHashtagList()
                .stream()
                .map(reviewHashtagPatchRequestDto -> reviewHashtagPatchRequestDto.toEntity(review))
                .collect(Collectors.toList()));

        return makeResult(HttpStatus.OK, review);
    }
}
