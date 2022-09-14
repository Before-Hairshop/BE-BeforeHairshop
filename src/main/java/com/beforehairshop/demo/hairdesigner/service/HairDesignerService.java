package com.beforehairshop.demo.hairdesigner.service;

import com.beforehairshop.demo.aws.S3Uploader;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerHashtag;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerPrice;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerWorkingDay;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerDetailGetResponseDto;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerProfilePatchRequestDto;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerProfileSaveRequestDto;
import com.beforehairshop.demo.hairdesigner.repository.HairDesignerHashtagRepository;
import com.beforehairshop.demo.hairdesigner.repository.HairDesignerPriceRepository;
import com.beforehairshop.demo.hairdesigner.repository.HairDesignerProfileRepository;
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

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import static com.beforehairshop.demo.response.ResultDto.*;

@Service
@Slf4j
@AllArgsConstructor
public class HairDesignerService {

    private final HairDesignerProfileRepository hairDesignerProfileRepository;
    private final HairDesignerWorkingDayRepository hairDesignerWorkingDayRepository;
    private final HairDesignerPriceRepository hairDesignerPriceRepository;
    private final HairDesignerHashtagRepository hairDesignerHashtagRepository;
    private final MemberRepository memberRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public ResponseEntity<ResultDto> save(Member member, HairDesignerProfileSaveRequestDto hairDesignerProfileSaveRequestDto) throws IOException {
        Member hairDesigner = memberRepository.findById(member.getId()).orElse(null);
        if (hairDesigner == null)
            return makeResult(HttpStatus.BAD_REQUEST, "id 값으로 불러온 member 가 null 입니다. id 값을 확인해주세요");

        if (hairDesigner.getDesignerFlag() == 1)
            return makeResult(HttpStatus.BAD_REQUEST, "이 유저는 이미 헤어 디자이너입니다.");

        hairDesigner.setDesignerFlag(1);
        String imageUrl = s3Uploader.upload(hairDesignerProfileSaveRequestDto.getImage()
                , hairDesigner.getId() + "/profile.jpg");

        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.save(hairDesignerProfileSaveRequestDto.toEntity(hairDesigner, imageUrl));

        /**
         * 헤어 디자이너의 해쉬 태그(entity)에 대한 row 생성
         */
        hairDesignerHashtagRepository.saveAll(
                hairDesignerProfileSaveRequestDto.getHashtagList()
                        .stream()
                        .map(hairDesignerHashtagSaveRequestDto -> hairDesignerHashtagSaveRequestDto.toEntity(hairDesigner))
                        .collect(Collectors.toList())
        );

        /**
         * 헤어 디자이너가 일하는 시간(entity)에 대한 row 생성
         */
        hairDesignerWorkingDayRepository.saveAll(
                hairDesignerProfileSaveRequestDto.getWorkingDayList()
                        .stream()
                        .map(hairDesignerWorkingDaySaveRequestDto -> hairDesignerWorkingDaySaveRequestDto.toEntity(hairDesigner))
                        .collect(Collectors.toList())
        );

        /**
         *  헤어 디자이너의 스타일링 비용(entity)에 대한 row 생성
         */
        hairDesignerPriceRepository.saveAll(
                hairDesignerProfileSaveRequestDto.getPriceList()
                        .stream()
                        .map(hairDesignerPriceSaveRequestDto -> hairDesignerPriceSaveRequestDto.toEntity(hairDesigner))
                        .collect(Collectors.toList()));


        return makeResult(HttpStatus.OK, hairDesignerProfile);
    }

    @Transactional
    public ResponseEntity<ResultDto> findOne(Member member, BigInteger hairDesignerId) {
        Member designer = memberRepository.findById(hairDesignerId).orElse(null);

        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByMember(designer).orElse(null);
        if (hairDesignerProfile == null)
            return makeResult(HttpStatus.BAD_REQUEST, "해당 id 값을 가지는 member 는 없습니다.");

        List<HairDesignerHashtag> hairDesignerHashtagList = hairDesignerHashtagRepository.findAllByHairDesigner(designer);
        List<HairDesignerWorkingDay> hairDesignerWorkingDayList = hairDesignerWorkingDayRepository.findAllByHairDesigner(member);
        List<HairDesignerPrice> hairDesignerPriceList = hairDesignerPriceRepository.findAllByHairDesigner(member);

        /**
         * 별점, 리뷰 정보 가져오는 부분 추가해야 함!
         */

        return makeResult(HttpStatus.OK, new HairDesignerDetailGetResponseDto(hairDesignerProfile
                , hairDesignerHashtagList
                , hairDesignerWorkingDayList
                , hairDesignerPriceList));
    }

    @Transactional
    public ResponseEntity<ResultDto> findMany(Pageable pageable) {
        /**
         * 유저의 위치 정보가 아직 고려되어 있지 않음.
         */

        /**
         * 별점 순 혹은 리뷰 순으로 정렬해주는 기능 빠짐.
         */
        Page<HairDesignerProfile> hairDesigners = hairDesignerProfileRepository.findAll(pageable);

        return makeResult(HttpStatus.OK, hairDesigners);

    }

    public ResponseEntity<ResultDto> patchOne(Member hairDesigner
            , HairDesignerProfilePatchRequestDto patchDto) throws IOException {
        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByMember(hairDesigner).orElse(null);
        if (hairDesignerProfile == null)
            return makeResult(HttpStatus.BAD_REQUEST, "해당 유저는 디자이너 프로필이 없습니다.");

        if (patchDto.getImage() != null)
           hairDesignerProfile.setImageUrl(s3Uploader.upload(patchDto.getImage(), hairDesigner.getId() + "/profile.jpg"));

        if (patchDto.getName() != null)
            hairDesignerProfile.setName(patchDto.getName());

        if (patchDto.getDescription() != null)
            hairDesignerProfile.setDescription(patchDto.getDescription());

        if (patchDto.getHairShopName() != null)
            hairDesignerProfile.setHairShopName(patchDto.getHairShopName());

        if (patchDto.getZipCode() != null) {
            hairDesignerProfile.setZipCode(patchDto.getZipCode());
            hairDesignerProfile.setZipAddress(patchDto.getZipAddress());
            hairDesignerProfile.setLatitude(patchDto.getLatitude());
            hairDesignerProfile.setLongitude(patchDto.getLongitude());
        }

        if (patchDto.getDetailAddress() != null)
            hairDesignerProfile.setDetailAddress(patchDto.getDetailAddress());

        if (patchDto.getPhoneNumber() != null)
            hairDesignerProfile.setPhoneNumber(patchDto.getPhoneNumber());

        return makeResult(HttpStatus.OK, hairDesignerProfile);
    }
}
