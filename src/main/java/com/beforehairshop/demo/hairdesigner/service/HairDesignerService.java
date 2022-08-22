package com.beforehairshop.demo.hairdesigner.service;

import com.beforehairshop.demo.hairdesigner.domain.HairDesigner;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerWorkingDay;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerDetailResponseDto;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerSaveRequestDto;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerWorkingDaySaveRequestDto;
import com.beforehairshop.demo.hairdesigner.repository.HairDesignerPriceRepository;
import com.beforehairshop.demo.hairdesigner.repository.HairDesignerRepository;
import com.beforehairshop.demo.hairdesigner.repository.HairDesignerWorkingDayRepository;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.repository.MemberRepository;
import com.beforehairshop.demo.response.ResultDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
@Slf4j
@AllArgsConstructor
public class HairDesignerService {

    private final HairDesignerRepository hairDesignerRepository;
    private final HairDesignerWorkingDayRepository hairDesignerWorkingDayRepository;
    private final HairDesignerPriceRepository hairDesignerPriceRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ResponseEntity<ResultDto> save(HairDesignerSaveRequestDto hairDesignerSaveRequestDto) {
        Member member = memberRepository.findById(hairDesignerSaveRequestDto.getMemberId()).orElse(null);
        if (member == null) return makeResult(HttpStatus.BAD_REQUEST, "id 값으로 불러온 member 가 null 입니다. id 값을 확인해주세요");

        member.setDesignerFlag(1);

        HairDesigner hairDesigner = hairDesignerRepository.save(hairDesignerSaveRequestDto.toEntity(member));

        try {
            for (HairDesignerWorkingDaySaveRequestDto hairDesignerWorkingDaySaveRequestDto : hairDesignerSaveRequestDto.getWorkingDayList()) {
                hairDesignerWorkingDayRepository.save(
                        hairDesignerWorkingDaySaveRequestDto.toEntity(hairDesigner)
                );
            }
        } catch (Exception exception) {
            return makeResult(HttpStatus.INTERNAL_SERVER_ERROR, "헤어 디자이너의 일하는 요일/시간에 대한 data 를 삽입하지 못했습니다.");
        }

        /**
         *  헤어 디자이너의 스타일링 비용(entity)에 대한 row 생성
         */
        hairDesignerPriceRepository.saveAll(
                hairDesignerSaveRequestDto.getPriceList()
                        .stream()
                        .map(hairDesignerPriceSaveRequestDto -> hairDesignerPriceSaveRequestDto.toEntity(member))
                        .collect(Collectors.toList()));


        return makeResult(HttpStatus.OK, hairDesigner);
    }

    @Transactional
    public ResponseEntity<ResultDto> findOne(BigInteger id) {
        Member member = memberRepository.findById(id).orElse(null);

        List<HairDesigner> hairDesignerList = hairDesignerRepository.findAllByMember(member);
        if (hairDesignerList == null || hairDesignerList.size() == 0) {
            return makeResult(HttpStatus.BAD_REQUEST, "해당 id 값을 가지는 member 는 없습니다.");
        }

        List<HairDesignerWorkingDay> hairDesignerWorkingDayList = hairDesignerWorkingDayRepository.findAllByHairDesigner(hairDesignerList.get(0));

        /**
         * 별점, 리뷰 정보 가져오는 부분 추가해야 함!
         */

        return makeResult(HttpStatus.OK, new HairDesignerDetailResponseDto(hairDesignerList.get(0), hairDesignerWorkingDayList));
    }

    @Transactional
    public ResponseEntity<ResultDto> findMany(Pageable pageable) {
        /**
         * 유저의 위치 정보가 아직 고려되어 있지 않음.
         */

        /**
         * 별점 순 혹은 리뷰 순으로 정렬해주는 기능 빠짐.
         */
        Page<HairDesigner> hairDesigners = hairDesignerRepository.findAll(pageable);

        return makeResult(HttpStatus.OK, hairDesigners);

    }
}
