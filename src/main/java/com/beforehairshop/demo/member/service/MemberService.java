package com.beforehairshop.demo.member.service;

import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.dto.MemberDto;
import com.beforehairshop.demo.member.dto.MemberSaveRequestDto;
import com.beforehairshop.demo.member.repository.MemberRepository;
import com.beforehairshop.demo.member.domain.StatusKind;
import com.beforehairshop.demo.response.ResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

import static com.beforehairshop.demo.response.ResultDto.makeResult;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public BigInteger save(MemberSaveRequestDto requestDto) {
        if (memberRepository.findOneByEmailAndStatusIsLessThan(requestDto.getEmail(), StatusKind.DELETE.getId()).orElse(null) != null) {
            log.error("이미 가입되어 있는 유저입니다.");
            return null;
        }

        Member member = memberRepository.save(requestDto.toEntity());

        return member.getId();
    }

}
