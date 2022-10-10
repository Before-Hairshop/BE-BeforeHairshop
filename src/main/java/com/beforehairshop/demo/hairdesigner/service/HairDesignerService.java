package com.beforehairshop.demo.hairdesigner.service;

import com.beforehairshop.demo.auth.handler.PrincipalDetailsUpdater;
import com.beforehairshop.demo.aws.S3Uploader;
import com.beforehairshop.demo.aws.handler.CloudFrontUrlHandler;
import com.beforehairshop.demo.aws.service.AmazonS3Service;
import com.beforehairshop.demo.constant.member.StatusKind;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerHashtagDto;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerPriceDto;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerProfileDto;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerWorkingDayDto;
import com.beforehairshop.demo.hairdesigner.dto.response.HairDesignerDetailGetResponseDto;
import com.beforehairshop.demo.hairdesigner.dto.patch.HairDesignerProfilePatchRequestDto;
import com.beforehairshop.demo.hairdesigner.dto.post.HairDesignerProfileSaveRequestDto;
import com.beforehairshop.demo.hairdesigner.dto.response.HairDesignerProfileAndHashtagDto;
import com.beforehairshop.demo.hairdesigner.dto.response.HairDesignerProfileImageResponseDto;
import com.beforehairshop.demo.hairdesigner.handler.PageOffsetHandler;
import com.beforehairshop.demo.hairdesigner.repository.HairDesignerHashtagRepository;
import com.beforehairshop.demo.hairdesigner.repository.HairDesignerPriceRepository;
import com.beforehairshop.demo.hairdesigner.repository.HairDesignerProfileRepository;
import com.beforehairshop.demo.hairdesigner.repository.HairDesignerWorkingDayRepository;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfile;
import com.beforehairshop.demo.member.dto.MemberDto;
import com.beforehairshop.demo.member.repository.MemberProfileRepository;
import com.beforehairshop.demo.member.repository.MemberRepository;
import com.beforehairshop.demo.response.ResultDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

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
    private final MemberProfileRepository memberProfileRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public ResponseEntity<ResultDto> save(Member member, HairDesignerProfileSaveRequestDto hairDesignerProfileSaveRequestDto) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");

        Member hairDesigner = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);
        if (hairDesigner == null)
            return makeResult(HttpStatus.BAD_REQUEST, "id 값으로 불러온 member 가 null 입니다. id 값을 확인해주세요");

        if (hairDesigner.getDesignerFlag() == 0)
            return makeResult(HttpStatus.BAD_REQUEST, "이 유저는 일반 유저입니다. 권한 변경을 먼저 해주시기 바랍니다.");

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

        // 권한 변경()
        PrincipalDetailsUpdater.setAuthenticationOfSecurityContext(hairDesigner, "ROLE_DESIGNER");

        return makeResult(HttpStatus.OK, new HairDesignerProfileDto(hairDesignerProfile));
    }

    @Transactional
    public ResponseEntity<ResultDto> findOne(Member member, BigInteger hairDesignerId) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");

        Member designer = memberRepository.findByIdAndStatus(hairDesignerId, StatusKind.NORMAL.getId()).orElse(null);

        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(designer, StatusKind.NORMAL.getId()).orElse(null);
        if (hairDesignerProfile == null)
            return makeResult(HttpStatus.BAD_REQUEST, "해당 id 값을 가지는 member 는 없습니다.");

        //List<HairDesignerHashtag> hairDesignerHashtagList = hairDesignerHashtagRepository.findAllByHairDesignerAndStatus(designer, StatusKind.NORMAL.getId());
        List<HairDesignerHashtagDto> hairDesignerHashtagDtoList
                = hairDesignerHashtagRepository.findAllByHairDesignerAndStatus(designer, StatusKind.NORMAL.getId())
                .stream()
                .map(hairDesignerHashtag -> new HairDesignerHashtagDto(hairDesignerHashtag))
                .collect(Collectors.toList());
        List<HairDesignerWorkingDayDto> hairDesignerWorkingDayDtoList
                = hairDesignerWorkingDayRepository.findAllByHairDesignerAndStatus(designer, StatusKind.NORMAL.getId())
                .stream()
                .map(hairDesignerWorkingDay -> new HairDesignerWorkingDayDto(hairDesignerWorkingDay))
                .collect(Collectors.toList());

        List<HairDesignerPriceDto> hairDesignerPriceDtoList
                = hairDesignerPriceRepository.findAllByHairDesignerAndStatus(designer, StatusKind.NORMAL.getId())
                .stream()
                .map(hairDesignerPrice -> new HairDesignerPriceDto(hairDesignerPrice))
                .collect(Collectors.toList());

        /**
         * 별점, 리뷰 정보 가져오는 부분 추가해야 함!
         */

        return makeResult(HttpStatus.OK, new HairDesignerDetailGetResponseDto(new HairDesignerProfileDto(hairDesignerProfile)
                , hairDesignerHashtagDtoList
                , hairDesignerWorkingDayDtoList
                , hairDesignerPriceDtoList));
    }

    @Transactional
    public ResponseEntity<ResultDto> findManyByLocation(Member member, Integer pageNumber) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");

        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null && hairDesignerProfile == null)
            return makeResult(HttpStatus.BAD_REQUEST, "이 유저의 프로필 등록이 되어있지 않습니다.");


        List<HairDesignerProfile> hairDesignerProfileList;
        if (memberProfile != null) {
            hairDesignerProfileList
                    = hairDesignerProfileRepository.findManyByLocationAndStatus(memberProfile.getLatitude(), memberProfile.getLongitude()
                    , new PageOffsetHandler().getOffsetByPageNumber(pageNumber), StatusKind.NORMAL.getId());
        }
        else {
            hairDesignerProfileList
                    = hairDesignerProfileRepository.findManyByLocationAndStatus(hairDesignerProfile.getLatitude(), hairDesignerProfile.getLongitude()
                    , new PageOffsetHandler().getOffsetByPageNumber(pageNumber), StatusKind.NORMAL.getId());

        }

        return makeResult(HttpStatus.OK, hairDesignerProfileList.stream().map(hairDesignerProfile1 -> new HairDesignerProfileDto(hairDesignerProfile1)).collect(Collectors.toList()));

    }

    @Transactional
    public ResponseEntity<ResultDto> patchOne(Member hairDesigner
            , HairDesignerProfilePatchRequestDto patchDto) {
        if (hairDesigner == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");

        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(hairDesigner, StatusKind.NORMAL.getId()).orElse(null);
        if (hairDesignerProfile == null)
            return makeResult(HttpStatus.BAD_REQUEST, "해당 유저는 디자이너 프로필이 없습니다.");

        Member updatedMember = memberRepository.findByIdAndStatus(hairDesigner.getId(), StatusKind.NORMAL.getId()).orElse(null);
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
            hairDesignerHashtagRepository.deleteAllInBatch(hairDesignerHashtagRepository.findAllByHairDesignerAndStatus(hairDesigner, StatusKind.NORMAL.getId()));

            hairDesignerHashtagRepository.saveAll(
                    patchDto.getHashtagPatchRequestDtoList()
                            .stream()
                            .map(hairDesignerHashtagPatchRequestDto -> hairDesignerHashtagPatchRequestDto.toEntity(hairDesigner))
                            .collect(Collectors.toList())
            );
        }

        if (patchDto.getPricePatchRequestDtoList() != null) {
            hairDesignerPriceRepository.deleteAllInBatch(hairDesignerPriceRepository.findAllByHairDesignerAndStatus(hairDesigner, StatusKind.NORMAL.getId()));

            hairDesignerPriceRepository.saveAll(
                    patchDto.getPricePatchRequestDtoList()
                            .stream()
                            .map(hairDesignerPricePatchRequestDto -> hairDesignerPricePatchRequestDto.toEntity(hairDesigner))
                            .collect(Collectors.toList())
            );
        }

        if (patchDto.getWorkingDayPatchRequestDtoList() != null) {
            hairDesignerWorkingDayRepository.deleteAllInBatch(hairDesignerWorkingDayRepository.findAllByHairDesignerAndStatus(hairDesigner, StatusKind.NORMAL.getId()));

            hairDesignerWorkingDayRepository.saveAll(
                    patchDto.getWorkingDayPatchRequestDtoList()
                            .stream()
                            .map(hairDesignerWorkingDayPatchRequestDto -> hairDesignerWorkingDayPatchRequestDto.toEntity(hairDesigner))
                            .collect(Collectors.toList())
            );
        }

        // 닉네임 변경
        PrincipalDetailsUpdater.setAuthenticationOfSecurityContext(updatedMember, "ROLE_DESIGNER");

        return makeResult(HttpStatus.OK, new HairDesignerProfileDto(hairDesignerProfile));
    }

    @Transactional
    public ResponseEntity<ResultDto> saveImage(Member member, AmazonS3Service amazonS3Service) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");

        Member hairDesigner = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);

        if (hairDesigner.getDesignerFlag() == 0)
            return makeResult(HttpStatus.BAD_REQUEST, "이 유저는 헤어 디자이너가 아닙니다.");

        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);

        if (hairDesignerProfile == null)
            return makeResult(HttpStatus.BAD_REQUEST, "해당 유저의 헤어 디자이너 프로필이 없습니다.");

        // presigned url 생성
        String preSignedUrl = amazonS3Service.generatePreSignedUrl(CloudFrontUrlHandler.getProfileOfDesignerS3Path(hairDesigner.getId()));

        hairDesignerProfile.setImageUrl(CloudFrontUrlHandler.getProfileOfDesignerImageUrl(hairDesigner.getId()));
        hairDesigner.setImageUrl(CloudFrontUrlHandler.getProfileOfDesignerImageUrl(hairDesigner.getId()));

        // 권한 변경 X (image url 변경)
        PrincipalDetailsUpdater.setAuthenticationOfSecurityContext(hairDesigner, "ROLE_DESIGNER");

        return makeResult(HttpStatus.OK, new HairDesignerProfileImageResponseDto(preSignedUrl));
    }

    @Transactional
    public ResponseEntity<ResultDto> findAllByName(Member member, String name, Pageable pageable) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");

        List<HairDesignerProfile> hairDesignerProfileList = hairDesignerProfileRepository.findAllByNameAndStatus(name
                , StatusKind.NORMAL.getId(), pageable);

        List<HairDesignerProfileAndHashtagDto> hairDesignerProfileAndHashtagDtoList = hairDesignerProfileList.stream()
                .map(hairDesignerProfile -> new HairDesignerProfileAndHashtagDto(new HairDesignerProfileDto(hairDesignerProfile)
                        , hairDesignerHashtagRepository.findAllByHairDesignerAndStatus(hairDesignerProfile.getHairDesigner(), StatusKind.NORMAL.getId()).stream()
                        .map(hairDesignerHashtag -> new HairDesignerHashtagDto(hairDesignerHashtag))
                        .collect(Collectors.toList())))
                .collect(Collectors.toList());

        return makeResult(HttpStatus.OK, hairDesignerProfileAndHashtagDtoList);
    }

    @Transactional
    public ResponseEntity<ResultDto> deleteProfile(Member member) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");

        Member designer = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);
        if (designer == null)
            return makeResult(HttpStatus.BAD_REQUEST, "유저의 세션이 만료되었거나, 잘못된 유저 정보입니다.");

        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (hairDesignerProfile == null)
            return makeResult(HttpStatus.BAD_REQUEST, "해당 유저에겐 헤어 디자이너 프로필이 존재하지 않습니다.");

        hairDesignerProfile.setStatus(StatusKind.DELETE.getId());
        designer.setImageUrl(null);

        // 권한 변경 X (image url 변경)
        PrincipalDetailsUpdater.setAuthenticationOfSecurityContext(designer, "ROLE_DESIGNER");

        return makeResult(HttpStatus.OK, new MemberDto(designer));
    }

    @Transactional
    public ResponseEntity<ResultDto> patchImage(Member member, AmazonS3Service amazonS3Service) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");

        String preSignedUrl = amazonS3Service.generatePreSignedUrl(CloudFrontUrlHandler.getProfileOfDesignerS3Path(member.getId()));

        return makeResult(HttpStatus.OK, new HairDesignerProfileImageResponseDto(preSignedUrl));
    }
}
