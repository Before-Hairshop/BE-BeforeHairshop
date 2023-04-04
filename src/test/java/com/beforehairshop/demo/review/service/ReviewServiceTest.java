package com.beforehairshop.demo.review.service;

import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.dto.MemberDto;
import com.beforehairshop.demo.member.service.MemberService;
import com.beforehairshop.demo.response.ResultDto;
import com.beforehairshop.demo.review.dto.ReviewDto;
import com.beforehairshop.demo.review.dto.ReviewHashtagDto;
import com.beforehairshop.demo.review.dto.ReviewImageDto;
import com.beforehairshop.demo.review.dto.patch.ReviewHashtagPatchRequestDto;
import com.beforehairshop.demo.review.dto.patch.ReviewPatchRequestDto;
import com.beforehairshop.demo.review.dto.response.ReviewDetailResponseDto;
import com.beforehairshop.demo.review.dto.save.ReviewHashtagSaveRequestDto;
import com.beforehairshop.demo.review.dto.save.ReviewSaveRequestDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReviewServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    ReviewService reviewService;

    @PersistenceContext
    EntityManager em;

    @BeforeEach
    public void initData() {
        List<ReviewHashtagSaveRequestDto> hashtagList = new ArrayList<>();
        hashtagList.add(new ReviewHashtagSaveRequestDto("스포츠컷"));
        hashtagList.add(new ReviewHashtagSaveRequestDto("박서준컷"));

        reviewService.save(new Member(BigInteger.valueOf(9)), new ReviewSaveRequestDto(
                BigInteger.valueOf(1), 5, 5, 5, "테스트 콘텐츠1",  hashtagList
        ));

        reviewService.save(new Member(BigInteger.valueOf(9)), new ReviewSaveRequestDto(
                BigInteger.valueOf(1), 5, 5, 5, "테스트 콘텐츠1",  hashtagList
        ));

        reviewService.save(new Member(BigInteger.valueOf(9)), new ReviewSaveRequestDto(
                BigInteger.valueOf(1), 5, 5, 5, "테스트 콘텐츠1",  hashtagList
        ));

        reviewService.save(new Member(BigInteger.valueOf(9)), new ReviewSaveRequestDto(
                BigInteger.valueOf(2), 5, 5, 5, "테스트 콘텐츠2",  hashtagList
        ));

        reviewService.save(new Member(BigInteger.valueOf(9)), new ReviewSaveRequestDto(
                BigInteger.valueOf(2), 5, 5, 5, "테스트 콘텐츠1",  hashtagList
        ));

        reviewService.save(new Member(BigInteger.valueOf(9)), new ReviewSaveRequestDto(
                BigInteger.valueOf(3), 5, 5, 5, "테스트 콘텐츠3",  hashtagList
        ));

        em.flush();
        em.clear();

        System.out.println("================================[init data]======================================");
    }

    @Test
    public void saveReview() {
        MemberDto findMemberDto = (MemberDto) memberService.findMeByDB(new Member(BigInteger.valueOf(9))).getBody().getResult();

        List<ReviewHashtagSaveRequestDto> hashtagList = new ArrayList<>();
        hashtagList.add(new ReviewHashtagSaveRequestDto("스포츠컷"));
        hashtagList.add(new ReviewHashtagSaveRequestDto("박서준컷"));

        ResponseEntity<ResultDto> response = reviewService.save(new Member(findMemberDto.getId()), new ReviewSaveRequestDto(
                BigInteger.valueOf(1), 5, 5, 5, "테스트 콘텐츠!",  hashtagList
        ));

        ReviewDto reviewDto = (ReviewDto) response.getBody().getResult();

        assertThat(reviewDto.getReviewerId()).isEqualTo(9);
        assertThat(reviewDto.getServiceRating()).isEqualTo(5);
        assertThat(reviewDto.getTotalRating()).isEqualTo(5);
        assertThat(reviewDto.getStyleRating()).isEqualTo(5);
        assertThat(reviewDto.getContent()).isEqualTo("테스트 콘텐츠!");
    }

    @Test
    public void findReviews() {
        ResponseEntity<ResultDto> response = reviewService.findManyByHairDesigner(new Member(BigInteger.valueOf(1)), BigInteger.valueOf(1), PageRequest.of(0, 2));
        List<ReviewDetailResponseDto> reviewList = (List<ReviewDetailResponseDto>) response.getBody().getResult();

        for (ReviewDetailResponseDto responseDto : reviewList) {
            assertThat(responseDto.getHashtagDtoList().size()).isEqualTo(2);

            assertThat(responseDto.getImageDtoList().size()).isEqualTo(0);
        }

        assertThat(reviewList.size()).isEqualTo(2);
    }

    // (기존) 1008ms, 980ms => (delete batch) 829ms, 889ms
    //
    @Test
    public void patchReview() {
        ArrayList<ReviewHashtagPatchRequestDto> hashtagList = new ArrayList<>();
        hashtagList.add(new ReviewHashtagPatchRequestDto("새로운컷1"));
        hashtagList.add(new ReviewHashtagPatchRequestDto("새로운컷2"));
        hashtagList.add(new ReviewHashtagPatchRequestDto("새로운컷3"));


        ResponseEntity<ResultDto> response = reviewService.patchOne(new Member(BigInteger.valueOf(9)), BigInteger.valueOf(1), new ReviewPatchRequestDto(4, 4, 4, "컨텐츠 수정후",
                hashtagList)
        );

        em.flush();
        em.clear();

        ReviewDto reviewDto = (ReviewDto) response.getBody().getResult();

        assertThat(reviewDto.getContent()).isEqualTo("컨텐츠 수정후");
        assertThat(reviewDto.getServiceRating()).isEqualTo(4);
        assertThat(reviewDto.getTotalRating()).isEqualTo(4);
        assertThat(reviewDto.getStyleRating()).isEqualTo(4);

    }


}