package com.beforehairshop.demo.member.service;

import com.beforehairshop.demo.constant.member.StatusKind;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfile;
import com.beforehairshop.demo.member.dto.MemberProfileDto;
import com.beforehairshop.demo.member.dto.post.MemberProfileSaveRequestDto;
import com.beforehairshop.demo.member.dto.post.MemberSaveRequestDto;
import com.beforehairshop.demo.member.dto.response.MemberProfileDetailResponseDto;
import com.beforehairshop.demo.member.repository.MemberProfileRepository;
import com.beforehairshop.demo.member.repository.MemberRepository;
import com.beforehairshop.demo.oauth.dto.post.AppleUserSaveRequestDto;
import com.beforehairshop.demo.oauth.service.OAuthService;
import com.beforehairshop.demo.response.ResultDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberProfileRepository memberProfileRepository;

    @Autowired
    OAuthService oAuthService;

    @PersistenceContext
    EntityManager em;

    @Autowired
    EntityManagerFactory emf;

    @Test
    public void findMyProfileTest() {
        Member member = new Member(BigInteger.valueOf(9));

        ResponseEntity<ResultDto> profileResponse = memberService.findMyProfile(member);
        MemberProfileDetailResponseDto memberProfileDto = (MemberProfileDetailResponseDto) profileResponse.getBody().getResult();

        assertThat(memberProfileDto.getMemberProfileDto().getMemberId()).isEqualTo(9);
    }

    @Test
    public void findMemberProfileBySomeone() {
        Member someone = new Member(BigInteger.valueOf(1));

        BigInteger memberProfileId = BigInteger.valueOf(1);
        ResponseEntity<ResultDto> response = memberService.findMemberProfile(someone, memberProfileId);

        MemberProfileDetailResponseDto responseDto = (MemberProfileDetailResponseDto) response.getBody().getResult();

        assertThat(responseDto.getMemberProfileDto().getId()).isEqualTo(memberProfileId);
    }

    @Test
    @DisplayName("MemberProfile 엔티티와 Member 엔티티를 fetch join 으로 조회되는지 테스트")
    public void memberAndMemberProfileByFetchJoinTest() {
        Optional<MemberProfile> memberProfile = memberProfileRepository.findMemberAndProfileByIdAndStatusUsingFetchJoin(BigInteger.valueOf(1), StatusKind.NORMAL.getId());

        assertThat(memberProfile).isNotNull();
        assertThat(emf.getPersistenceUnitUtil().isLoaded(memberProfile.get().getMember())).isEqualTo(true);
    }



}