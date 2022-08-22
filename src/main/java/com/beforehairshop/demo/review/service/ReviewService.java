package com.beforehairshop.demo.review.service;

import com.beforehairshop.demo.hairdesigner.domain.HairDesigner;
import com.beforehairshop.demo.hairdesigner.repository.HairDesignerRepository;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.repository.MemberRepository;
import com.beforehairshop.demo.response.ResultDto;
import com.beforehairshop.demo.review.domain.Review;
import com.beforehairshop.demo.review.dto.ReviewSaveRequestDto;
import com.beforehairshop.demo.review.repository.ReviewRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

import static com.beforehairshop.demo.response.ResultDto.*;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public ResponseEntity<ResultDto> save(ReviewSaveRequestDto reviewSaveRequestDto) {
        Member member = memberRepository.findById(reviewSaveRequestDto.getMemberId()).orElse(null);
        Member hairDesigner = memberRepository.findById(reviewSaveRequestDto.getHairDesignerId()).orElse(null);

        if (member != null && hairDesigner != null && hairDesigner.getDesignerFlag() != 1)
            return makeResult(HttpStatus.INTERNAL_SERVER_ERROR, "리뷰 대상이 헤어 디자이너가 아니다.");

        Review review = reviewRepository.save(
                reviewSaveRequestDto.toEntity(member, hairDesigner)
        );

        return makeResult(HttpStatus.OK, review);
    }

    public ResponseEntity<ResultDto> findManyByHairDesigner(BigInteger hairDesignerId, Pageable pageable) {
        List<Review> reviewList = reviewRepository.findAllByHairDesignerId(hairDesignerId, pageable);

        return makeResult(HttpStatus.OK, reviewList);
    }
}
