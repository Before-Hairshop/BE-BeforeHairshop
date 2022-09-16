package com.beforehairshop.demo.hairdesigner.service;

import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.aws.S3Uploader;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerHashtag;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerPrice;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerWorkingDay;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerDetailGetResponseDto;
import com.beforehairshop.demo.hairdesigner.dto.patch.HairDesignerProfilePatchRequestDto;
import com.beforehairshop.demo.hairdesigner.dto.post.HairDesignerProfileSaveRequestDto;
import com.beforehairshop.demo.hairdesigner.handler.PageOffsetHandler;
import com.beforehairshop.demo.hairdesigner.repository.HairDesignerHashtagRepository;
import com.beforehairshop.demo.hairdesigner.repository.HairDesignerPriceRepository;
import com.beforehairshop.demo.hairdesigner.repository.HairDesignerProfileRepository;
import com.beforehairshop.demo.hairdesigner.repository.HairDesignerWorkingDayRepository;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfile;
import com.beforehairshop.demo.member.repository.MemberProfileRepository;
import com.beforehairshop.demo.member.repository.MemberRepository;
import com.beforehairshop.demo.response.ResultDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
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
    private final MemberProfileRepository memberProfileRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public ResponseEntity<ResultDto> save(Member member, HairDesignerProfileSaveRequestDto hairDesignerProfileSaveRequestDto) {
        Member hairDesigner = memberRepository.findById(member.getId()).orElse(null);
        if (hairDesigner == null)
            return makeResult(HttpStatus.BAD_REQUEST, "id 값으로 불러온 member 가 null 입니다. id 값을 확인해주세요");

        if (hairDesigner.getDesignerFlag() == 1)
            return makeResult(HttpStatus.BAD_REQUEST, "이 유저는 이미 헤어 디자이너입니다.");

        hairDesigner.setDesignerFlag(1);
        hairDesigner.setRole("ROLE_DESIGNER");
        hairDesigner.setName(hairDesignerProfileSaveRequestDto.getName());

        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.save(hairDesignerProfileSaveRequestDto.toEntity(hairDesigner));

        /**
         * 헤어 디자이너의 해쉬 태그(entity)에 대한 row 생성
         */
        if (hairDesignerProfileSaveRequestDto.getHashtagList() != null) {
            hairDesignerHashtagRepository.saveAll(
                    hairDesignerProfileSaveRequestDto.getHashtagList()
                            .stream()
                            .map(hairDesignerHashtagSaveRequestDto -> hairDesignerHashtagSaveRequestDto.toEntity(hairDesigner))
                            .collect(Collectors.toList())
            );
        }

        /**
         * 헤어 디자이너가 일하는 시간(entity)에 대한 row 생성
         */
        if (hairDesignerProfileSaveRequestDto.getWorkingDayList() != null) {
            hairDesignerWorkingDayRepository.saveAll(
                    hairDesignerProfileSaveRequestDto.getWorkingDayList()
                            .stream()
                            .map(hairDesignerWorkingDaySaveRequestDto -> hairDesignerWorkingDaySaveRequestDto.toEntity(hairDesigner))
                            .collect(Collectors.toList())
            );
        }

        /**
         *  헤어 디자이너의 스타일링 비용(entity)에 대한 row 생성
         */
        if (hairDesignerProfileSaveRequestDto.getPriceList() != null) {
            hairDesignerPriceRepository.saveAll(
                    hairDesignerProfileSaveRequestDto.getPriceList()
                            .stream()
                            .map(hairDesignerPriceSaveRequestDto -> hairDesignerPriceSaveRequestDto.toEntity(hairDesigner))
                            .collect(Collectors.toList()));
        }

        // ROLE 권한 변경

//        Collection<GrantedAuthority> updatedAuthorities = new ArrayList<>();
//        updatedAuthorities.add(new GrantedAuthority() {
//            @Override
//            public String getAuthority() {
//                return member.getRole();
//            }
//        });

        List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
        updatedAuthorities.add(new SimpleGrantedAuthority("ROLE_DESIGNER"));

//        아래는 안되는 코드
//        Authentication authentication = new UsernamePasswordAuthenticationToken(hairDesigner, hairDesigner.getPassword(), updatedAuthorities);

        Authentication authentication = new UsernamePasswordAuthenticationToken(new PrincipalDetails(hairDesigner), null, updatedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return makeResult(HttpStatus.OK, hairDesignerProfile);
    }

    @Transactional
    public ResponseEntity<ResultDto> findOne(Member member, BigInteger hairDesignerId) {
        Member designer = memberRepository.findById(hairDesignerId).orElse(null);

        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesigner(designer).orElse(null);
        if (hairDesignerProfile == null)
            return makeResult(HttpStatus.BAD_REQUEST, "해당 id 값을 가지는 member 는 없습니다.");

        List<HairDesignerHashtag> hairDesignerHashtagList = hairDesignerHashtagRepository.findAllByHairDesigner(designer);
        List<HairDesignerWorkingDay> hairDesignerWorkingDayList = hairDesignerWorkingDayRepository.findAllByHairDesigner(designer);
        List<HairDesignerPrice> hairDesignerPriceList = hairDesignerPriceRepository.findAllByHairDesigner(designer);

        /**
         * 별점, 리뷰 정보 가져오는 부분 추가해야 함!
         */

        return makeResult(HttpStatus.OK, new HairDesignerDetailGetResponseDto(hairDesignerProfile
                , hairDesignerHashtagList
                , hairDesignerWorkingDayList
                , hairDesignerPriceList));
    }

    @Transactional
    public ResponseEntity<ResultDto> findManyByLocation(Member member, Integer pageNumber) {

        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, 1).orElse(null);
        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(member, 1).orElse(null);
        if (memberProfile == null && hairDesignerProfile == null)
            return makeResult(HttpStatus.BAD_REQUEST, "이 유저의 프로필 등록이 되어있지 않습니다.");


        List<HairDesignerProfile> hairDesignerProfileList;
        if (memberProfile != null) {
            hairDesignerProfileList
                    = hairDesignerProfileRepository.findManyByLocation(memberProfile.getLatitude(), memberProfile.getLongitude(), new PageOffsetHandler().getOffsetByPageNumber(pageNumber));
        }
        else {
            hairDesignerProfileList
                    = hairDesignerProfileRepository.findManyByLocation(hairDesignerProfile.getLatitude(), hairDesignerProfile.getLongitude(), new PageOffsetHandler().getOffsetByPageNumber(pageNumber));

        }

        return makeResult(HttpStatus.OK, hairDesignerProfileList);

    }

    @Transactional
    public ResponseEntity<ResultDto> patchOne(Member hairDesigner
            , HairDesignerProfilePatchRequestDto patchDto) {
        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesigner(hairDesigner).orElse(null);
        if (hairDesignerProfile == null)
            return makeResult(HttpStatus.BAD_REQUEST, "해당 유저는 디자이너 프로필이 없습니다.");

        Member updatedMember = memberRepository.findById(hairDesigner.getId()).orElse(null);
        if (patchDto.getName() != null) {

            hairDesignerProfile.setName(patchDto.getName());
            updatedMember.setName(patchDto.getName());
        }

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


        if (patchDto.getHashtagPatchRequestDtoList() != null) {
            hairDesignerHashtagRepository.deleteAllInBatch(hairDesignerHashtagRepository.findAllByHairDesigner(hairDesigner));

            hairDesignerHashtagRepository.saveAll(
                    patchDto.getHashtagPatchRequestDtoList()
                            .stream()
                            .map(hairDesignerHashtagPatchRequestDto -> hairDesignerHashtagPatchRequestDto.toEntity(hairDesigner))
                            .collect(Collectors.toList())
            );
        }

        if (patchDto.getPricePatchRequestDtoList() != null) {
            hairDesignerPriceRepository.deleteAllInBatch(hairDesignerPriceRepository.findAllByHairDesigner(hairDesigner));

            hairDesignerPriceRepository.saveAll(
                    patchDto.getPricePatchRequestDtoList()
                            .stream()
                            .map(hairDesignerPricePatchRequestDto -> hairDesignerPricePatchRequestDto.toEntity(hairDesigner))
                            .collect(Collectors.toList())
            );
        }

        if (patchDto.getWorkingDayPatchRequestDtoList() != null) {
            hairDesignerWorkingDayRepository.deleteAllInBatch(hairDesignerWorkingDayRepository.findAllByHairDesigner(hairDesigner));

            hairDesignerWorkingDayRepository.saveAll(
                    patchDto.getWorkingDayPatchRequestDtoList()
                            .stream()
                            .map(hairDesignerWorkingDayPatchRequestDto -> hairDesignerWorkingDayPatchRequestDto.toEntity(hairDesigner))
                            .collect(Collectors.toList())
            );
        }

        // 닉네임 변경
        List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
        updatedAuthorities.add(new SimpleGrantedAuthority("ROLE_DESIGNER"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(new PrincipalDetails(updatedMember), null, updatedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return makeResult(HttpStatus.OK, hairDesignerProfile);
    }

    @Transactional
    public ResponseEntity<ResultDto> saveImage(Member member, MultipartFile image) throws IOException {
        Member hairDesigner = memberRepository.findById(member.getId()).orElse(null);
        if (hairDesigner == null)
            return makeResult(HttpStatus.BAD_REQUEST, "해당 유저의 세션이 만료됐습니다.");

        if (hairDesigner.getDesignerFlag() == 1)
            return makeResult(HttpStatus.BAD_REQUEST, "이 유저는 헤어 디자이너가 아닙니다.");

        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesigner(member).orElse(null);

        if (hairDesignerProfile == null)
            return makeResult(HttpStatus.BAD_REQUEST, "해당 유저의 헤어 디자이너 프로필이 없습니다.");

        String imageUrl = s3Uploader.upload(image
                , hairDesigner.getId() + "/profile.jpg");

        hairDesignerProfile.setImageUrl(imageUrl);
        hairDesigner.setImageUrl(imageUrl);

        // 닉네임 변경
        List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
        updatedAuthorities.add(new SimpleGrantedAuthority("ROLE_DESIGNER"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(new PrincipalDetails(hairDesigner), null, updatedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return makeResult(HttpStatus.OK, hairDesigner);
    }
}
