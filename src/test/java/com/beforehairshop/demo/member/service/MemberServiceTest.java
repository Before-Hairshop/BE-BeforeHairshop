package com.beforehairshop.demo.member.service;

import com.beforehairshop.demo.constant.member.StatusKind;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfile;
import com.beforehairshop.demo.member.dto.MemberProfileDto;
import com.beforehairshop.demo.member.dto.post.MemberProfileSaveRequestDto;
import com.beforehairshop.demo.member.dto.post.MemberSaveRequestDto;
import com.beforehairshop.demo.member.dto.response.MemberProfileDetailResponseDto;
import com.beforehairshop.demo.oauth.dto.post.AppleUserSaveRequestDto;
import com.beforehairshop.demo.oauth.service.OAuthService;
import com.beforehairshop.demo.response.ResultDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    OAuthService oAuthService;

    @PersistenceContext
    EntityManager em;

    @Test
    public void findProfileTest() {
        Member member = new Member(BigInteger.valueOf(9));

        ResponseEntity<ResultDto> profileResponse = memberService.findMyProfile(member);
        MemberProfileDetailResponseDto memberProfileDto = (MemberProfileDetailResponseDto) profileResponse.getBody().getResult();

        assertThat(memberProfileDto.getMemberProfileDto().getMemberId()).isEqualTo(9);
    }

}