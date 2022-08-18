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
        if (member == null) return makeResult(HttpStatus.BAD_REQUEST, "id 값으로 불러온 member 가 null 입니다. id 값을 확인해주세요");

        member.setDesigner_flag(1);

        HairDesigner hairDesigner = hairDesignerRepository.save(hairDesignerSaveRequestDto.toEntity(member));

        return makeResult(HttpStatus.OK, hairDesigner);
    }

    @Transactional
    public ResponseEntity<ResultDto> findOne(Long id) {
        Member member = memberRepository.findById(id).orElse(null);


    }
}
