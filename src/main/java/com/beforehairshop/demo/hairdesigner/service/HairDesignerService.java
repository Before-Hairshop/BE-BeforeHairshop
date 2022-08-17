package com.beforehairshop.demo.hairdesigner.service;

import com.beforehairshop.demo.hairdesigner.domain.HairDesigner;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerSaveRequestDto;
import com.beforehairshop.demo.hairdesigner.repository.HairDesignerRepository;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.repository.MemberRepository;
import com.beforehairshop.demo.response.ResultDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import static com.beforehairshop.demo.response.ResultDto.*;

@Service
@Slf4j
@AllArgsConstructor
public class HairDesignerService {

    private final HairDesignerRepository hairDesignerRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ResponseEntity<ResultDto> save(HairDesignerSaveRequestDto hairDesignerSaveRequestDto) {
        Member member = memberRepository.findById(hairDesignerSaveRequestDto.getMemberId()).orElse(null);

        HairDesigner hairDesigner = hairDesignerSaveRequestDto.toEntity(member);

        return makeResult(HttpStatus.OK, hairDesigner);
    }

}
