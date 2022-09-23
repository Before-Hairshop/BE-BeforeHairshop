package com.beforehairshop.demo.member.service;

import com.beforehairshop.demo.auth.handler.PrincipalDetailsUpdater;
import com.beforehairshop.demo.aws.service.AmazonS3Service;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.hairdesigner.handler.PageOffsetHandler;
import com.beforehairshop.demo.hairdesigner.repository.HairDesignerProfileRepository;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfile;
import com.beforehairshop.demo.member.domain.MemberProfileDesiredHairstyle;
import com.beforehairshop.demo.member.domain.MemberProfileDesiredHairstyleImage;
import com.beforehairshop.demo.member.dto.*;
import com.beforehairshop.demo.aws.handler.CloudFrontUrlHandler;
import com.beforehairshop.demo.member.repository.MemberProfileDesiredHairstyleImageRepository;
import com.beforehairshop.demo.member.repository.MemberProfileDesiredHairstyleRepository;
import com.beforehairshop.demo.member.repository.MemberProfileRepository;
import com.beforehairshop.demo.member.repository.MemberRepository;
import com.beforehairshop.demo.constant.StatusKind;
import com.beforehairshop.demo.response.ResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.beforehairshop.demo.response.ResultDto.makeResult;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final MemberProfileDesiredHairstyleRepository memberProfileDesiredHairstyleRepository;

    private final MemberProfileDesiredHairstyleImageRepository memberProfileDesiredHairstyleImageRepository;

    private final HairDesignerProfileRepository hairDesignerProfileRepository;

    @Transactional
    public BigInteger save(MemberSaveRequestDto requestDto) {
        if (memberRepository.findOneByEmailAndStatus(requestDto.getEmail(), StatusKind.NORMAL.getId()).orElse(null) != null) {
            log.error("이미 가입되어 있는 유저입니다.");
            return null;
        }

        Member member = memberRepository.save(requestDto.toEntity());

        return member.getId();
    }

//    @Transactional
//    public ResponseEntity<ResultDto> saveMemberProfileImages(Member member, MemberProfileImageSaveRequestDto memberProfileImageSaveRequestDto, S3Uploader s3Uploader) throws IOException {
//
//    }

    @Transactional
    public ResponseEntity<ResultDto> saveMemberProfile(Member member, MemberProfileSaveRequestDto memberProfileSaveRequestDto) {
        // 이미 존재한다면, bad request 처리해준다.
        if (memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null) != null)
            return makeResult(HttpStatus.BAD_REQUEST, "해당 유저의 프로필은 이미 존재합니다. 유저 당 하나의 프로필만 존재할 수 있습니다.");

        if (memberProfileSaveRequestDto.getName() == null)
            return makeResult(HttpStatus.BAD_REQUEST, "닉네임을 입력하지 않았습니다.");

        Member updatedMember = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);
        if (updatedMember == null)
            return makeResult(HttpStatus.BAD_REQUEST, "이 유저는 유효한 유저가 아닙니다.");

        updatedMember.setName(memberProfileSaveRequestDto.getName());

        // 닉네임 변경
        PrincipalDetailsUpdater.setAuthenticationOfSecurityContext(updatedMember, "ROLE_USER");

        MemberProfile memberProfile = memberProfileSaveRequestDto.toEntity(updatedMember, null, null, null);

        memberProfileRepository.save(memberProfile);

        /**
         * 유저가 원하는 헤어 스타일들에 대한 row 저장!
         */
        if (memberProfileSaveRequestDto.getDesiredHairstyleList() != null) {
            memberProfileDesiredHairstyleRepository.saveAll(
                    memberProfileSaveRequestDto.getDesiredHairstyleList()
                            .stream()
                            .map(desiredHairstyleSaveRequestDto -> desiredHairstyleSaveRequestDto.toEntity(memberProfile))
                            .collect(Collectors.toList())
            );
        }

        return makeResult(HttpStatus.OK, memberProfile);
    }

    @Transactional
    public ResponseEntity<ResultDto> findMyProfile(Member member) {
        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null) {
            return makeResult(HttpStatus.BAD_REQUEST, "해당 유저의 프로필은 존재하지 않습니다.");
        }

        List<MemberProfileDesiredHairstyle> desiredHairstyleList
                = memberProfileDesiredHairstyleRepository.findByMemberProfileAndStatus(memberProfile, StatusKind.NORMAL.getId());

        List<MemberProfileDesiredHairstyleImage> desiredHairstyleImageList
                = memberProfileDesiredHairstyleImageRepository.findByMemberProfileAndStatus(memberProfile, StatusKind.NORMAL.getId());
        return makeResult(HttpStatus.OK, new MemberProfileDetailResponseDto(memberProfile, desiredHairstyleList, desiredHairstyleImageList));
    }

    @Transactional
    public ResponseEntity<ResultDto> patchMyProfile(Member member, MemberProfilePatchRequestDto patchDto) {
        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null) {
            return makeResult(HttpStatus.BAD_REQUEST, "해당 유저의 프로필은 존재하지 않습니다. 먼저 만들고 난 뒤 수정해야합니다.");
        }

        Member updatedMember = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);
        if (updatedMember == null)
            return makeResult(HttpStatus.BAD_REQUEST, "유저의 세션이 만료되었거나 유효하지 않은 유저입니다.");

        if (patchDto.getName() != null) {
            memberProfile.setName(patchDto.getName());
            updatedMember.setName(patchDto.getName());
        }

        if (patchDto.getHairCondition() != null)
            memberProfile.setHairCondition(patchDto.getHairCondition());

        if (patchDto.getHairTendency() != null)
            memberProfile.setHairTendency(patchDto.getHairTendency());

        if (patchDto.getDesiredHairstyleDescription() != null)
            memberProfile.setDesiredHairstyleDescription(memberProfile.getDesiredHairstyleDescription());

        if (patchDto.getPayableAmount() != null)
            memberProfile.setPayableAmount(patchDto.getPayableAmount());

        if (patchDto.getZipCode() != null) {
            memberProfile.setZipCode(patchDto.getZipCode());
            memberProfile.setZipAddress(patchDto.getZipAddress());
            memberProfile.setLatitude(patchDto.getLatitude());
            memberProfile.setLongitude(patchDto.getLongitude());
        }

        if (patchDto.getDetailAddress() != null)
            memberProfile.setDetailAddress(patchDto.getDetailAddress());

        if (patchDto.getDesiredHairstyleList() != null) {
            memberProfileDesiredHairstyleRepository.deleteAllInBatch(
                    memberProfileDesiredHairstyleRepository.findByMemberProfileAndStatus(memberProfile, StatusKind.NORMAL.getId())
            );

            memberProfileDesiredHairstyleRepository.saveAll(
                    patchDto.getDesiredHairstyleList()
                            .stream()
                            .map(desiredHairstylePatchRequestDto -> desiredHairstylePatchRequestDto.toEntity(memberProfile))
                            .collect(Collectors.toList())
            );
        }

        // 닉네임 변경
        PrincipalDetailsUpdater.setAuthenticationOfSecurityContext(updatedMember, "ROLE_USER");

        memberProfile.setMember(updatedMember);

        return makeResult(HttpStatus.OK, memberProfile);
    }

    public ResponseEntity<ResultDto> findMe(Member member) {
        Member memberByDB = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);
        if (memberByDB == null)
            return makeResult(HttpStatus.BAD_REQUEST, "DB에 존재하지 않는 유저이거나, 유효하지 않은 유저이거나, 잘못된 세션 값으로 요청했습니다.");

        return makeResult(HttpStatus.OK, memberByDB);
    }

    @Transactional
    public ResponseEntity<ResultDto> changeToDesigner(Member member) {
        Member updatedMember = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);

        if (updatedMember == null)
            return makeResult(HttpStatus.BAD_REQUEST, "세션이 만료되었거나 삭제된 유저입니다.");

        updatedMember.setDesignerFlag(1);
        updatedMember.setRole("ROLE_DESIGNER");
        updatedMember.setImageUrl(null);

        PrincipalDetailsUpdater.setAuthenticationOfSecurityContext(updatedMember, "ROLE_DESIGNER");
        return makeResult(HttpStatus.OK, updatedMember);
    }

    @Transactional
    public ResponseEntity<ResultDto> changeToUser(Member member) {
        Member updatedMember = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);

        if (updatedMember == null)
            return makeResult(HttpStatus.BAD_REQUEST, "세션이 만료되었거나 삭제된 유저입니다.");

        updatedMember.setDesignerFlag(0);
        updatedMember.setRole("ROLE_USER");
        updatedMember.setImageUrl(null);

        PrincipalDetailsUpdater.setAuthenticationOfSecurityContext(updatedMember, "ROLE_USER");
        return makeResult(HttpStatus.OK, updatedMember);
    }

    @Transactional
    public ResponseEntity<ResultDto> validation(Member member, Integer hairDesignerFlag) {
        Member updatedMember = memberRepository.findByIdAndStatus(member.getId(), StatusKind.ABNORMAL.getId()).orElse(null);
        if (updatedMember == null)
            return makeResult(HttpStatus.BAD_REQUEST, "세션이 만료되었거나, 이미 정상인 유저입니다.");

        if (hairDesignerFlag != 1 && hairDesignerFlag != 0)
            return makeResult(HttpStatus.BAD_REQUEST, "헤어 디자이너 플래그 값은 0 혹은 1입니다.");

        updatedMember.setStatus(StatusKind.NORMAL.getId());
        updatedMember.setDesignerFlag(hairDesignerFlag);


        PrincipalDetailsUpdater.setAuthenticationOfSecurityContext(updatedMember, hairDesignerFlag == 1 ? "ROLE_DESIGNER" : "ROLE_USER");
        return makeResult(HttpStatus.OK, updatedMember);
    }


    @Transactional
    public ResponseEntity<ResultDto> saveMemberProfileImage(Member member, Integer frontImageFlag, Integer sideImageFlag, Integer backImageFlag
            , Integer desiredHairstyleImageCount, AmazonS3Service amazonS3Service) {
        if (frontImageFlag != 1)
            return makeResult(HttpStatus.BAD_REQUEST, "Front image 는 무조건 입력해야 합니다.");

        Member updatedMember = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);

        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null)
            return makeResult(HttpStatus.BAD_REQUEST, "이미지 등록 전, 프로필을 등록해야 합니다");

        String frontPreSignedUrl = amazonS3Service.generatePreSignedUrl(
                CloudFrontUrlHandler.getProfileOfUserS3Path(member.getId(), "front")
        );

        memberProfile.setFrontImageUrl(CloudFrontUrlHandler.getProfileOfUserImageUrl(member.getId(), "front"));
        updatedMember.setImageUrl(CloudFrontUrlHandler.getProfileOfUserImageUrl(member.getId(), "front"));

        String sidePreSignedUrl = null, backPreSignedUrl = null;
        if (sideImageFlag == 1) {
            sidePreSignedUrl = amazonS3Service.generatePreSignedUrl(CloudFrontUrlHandler.getProfileOfUserS3Path(member.getId(), "side"));
            memberProfile.setSideImageUrl(CloudFrontUrlHandler.getProfileOfUserImageUrl(member.getId(), "side"));
        }
        if (backImageFlag == 1) {
            backPreSignedUrl = amazonS3Service.generatePreSignedUrl(CloudFrontUrlHandler.getProfileOfUserS3Path(member.getId(), "back"));
            memberProfile.setBackImageUrl(CloudFrontUrlHandler.getProfileOfUserImageUrl(member.getId(), "back"));
        }

        PrincipalDetailsUpdater.setAuthenticationOfSecurityContext(updatedMember, "ROLE_USER");


        // 원하는 스타일 이미지 생성
        List<String> desiredHairstyleImagePreSignedUrlList = new ArrayList<>();
        for (int i = 0; i < desiredHairstyleImageCount; i++) {
            MemberProfileDesiredHairstyleImage imageEntity
                    = MemberProfileDesiredHairstyleImage.builder()
                    .memberProfile(memberProfile)
                    .imageUrl(null)
                    .status(1)
                    .build();

            imageEntity = memberProfileDesiredHairstyleImageRepository.save(imageEntity);
            String preSignedUrl = amazonS3Service.generatePreSignedUrl(
                    CloudFrontUrlHandler.getProfileOfUserDesiredStyleS3Path(memberProfile.getId(), imageEntity.getId())
            );

            desiredHairstyleImagePreSignedUrlList.add(preSignedUrl);

            // image Url 수정
            imageEntity.setImageUrl(CloudFrontUrlHandler.getProfileOfUserDesiredStyleImageUrl(memberProfile.getId(), imageEntity.getId()));
        }

        return makeResult(HttpStatus.OK, new MemberProfileImageResponseDto(frontPreSignedUrl, sidePreSignedUrl, backPreSignedUrl, desiredHairstyleImagePreSignedUrlList));
    }

    @Transactional
    public ResponseEntity<ResultDto> patchMyProfileImage(Member member, Integer frontImageFlag, Integer sideImageFlag, Integer backImageFlag
            , Integer addDesiredHairstyleImageCount, String[] deleteImageUrlList, AmazonS3Service amazonS3Service) {
        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);

        if (memberProfile == null)
            return makeResult(HttpStatus.BAD_REQUEST, "해당 유저에게는 유저 프로필이 등록되어 있지 않습니다.");

        String frontPreSignedUrl = null, sidePreSignedUrl = null, backPreSignedUrl = null;

        if (frontImageFlag == 1) {
            frontPreSignedUrl = amazonS3Service.generatePreSignedUrl(CloudFrontUrlHandler.getProfileOfUserS3Path(member.getId(), "front"));
        }

        if (sideImageFlag == 1) {
            sidePreSignedUrl = amazonS3Service.generatePreSignedUrl(CloudFrontUrlHandler.getProfileOfUserS3Path(member.getId(), "side"));
            memberProfile.setSideImageUrl(CloudFrontUrlHandler.getProfileOfUserImageUrl(member.getId(), "side"));
        }
        if (backImageFlag == 1) {
            backPreSignedUrl = amazonS3Service.generatePreSignedUrl(CloudFrontUrlHandler.getProfileOfUserS3Path(member.getId(), "back"));
            memberProfile.setBackImageUrl(CloudFrontUrlHandler.getProfileOfUserImageUrl(member.getId(), "back"));
        }

        // 원하는 스타일 이미지 중 삭제할 이미지 삭제
        for (int i = 0; i < deleteImageUrlList.length; i++) {
            MemberProfileDesiredHairstyleImage desiredHairstyleImage
                    = memberProfileDesiredHairstyleImageRepository.findByImageUrlAndStatus(deleteImageUrlList[i], StatusKind.NORMAL.getId()).orElse(null);

            if (desiredHairstyleImage == null)
                return makeResult(HttpStatus.BAD_REQUEST, "존재하지 않는 image url 입니다.");

            memberProfileDesiredHairstyleImageRepository.delete(desiredHairstyleImage);
        }

        // 추가할 이미지의 pre signed url 만들어서 리턴해준다.
        List<String> desiredHairstyleImagePreSignedUrlList = new ArrayList<>();
        for (int i = 0; i < addDesiredHairstyleImageCount; i++) {
            MemberProfileDesiredHairstyleImage imageEntity
                    = MemberProfileDesiredHairstyleImage.builder()
                    .memberProfile(memberProfile)
                    .imageUrl(null)
                    .status(StatusKind.NORMAL.getId())
                    .build();

            imageEntity = memberProfileDesiredHairstyleImageRepository.save(imageEntity);
            String preSignedUrl = amazonS3Service.generatePreSignedUrl(
                    CloudFrontUrlHandler.getProfileOfUserDesiredStyleS3Path(memberProfile.getId(), imageEntity.getId())
            );

            desiredHairstyleImagePreSignedUrlList.add(preSignedUrl);

            // image Url 수정
            imageEntity.setImageUrl(CloudFrontUrlHandler.getProfileOfUserDesiredStyleImageUrl(memberProfile.getId(), imageEntity.getId()));
        }

        return makeResult(HttpStatus.OK, new MemberProfileImageResponseDto(frontPreSignedUrl, sidePreSignedUrl, backPreSignedUrl
                , desiredHairstyleImagePreSignedUrlList));
    }

    @Transactional
    public ResponseEntity<ResultDto> findManyProfileByLocation(Member designer, Integer pageNumber) {
        Member hairDesigner = memberRepository.findByIdAndStatus(designer.getId(), StatusKind.NORMAL.getId()).orElse(null);
        if (hairDesigner == null || hairDesigner.getDesignerFlag() != 1)
            return makeResult(HttpStatus.BAD_REQUEST, "헤어 디자이너가 아니거나, 저장된 유저가 아닙니다.");

        HairDesignerProfile hairDesignerProfile
                = hairDesignerProfileRepository.findByHairDesignerAndStatus(designer, StatusKind.NORMAL.getId()).orElse(null);

        if (hairDesignerProfile == null)
            return makeResult(HttpStatus.BAD_REQUEST, "헤어 디자이너가 프로필이 없습니다.");

        List<MemberProfile> memberProfileList
                = memberProfileRepository.findManyByLocationAndStatus(hairDesignerProfile.getLatitude(), hairDesignerProfile.getLongitude()
                , new PageOffsetHandler().getOffsetByPageNumber(pageNumber), StatusKind.NORMAL.getId());

        List<MemberProfileListResponseDto> memberProfileListResponseDtoList = memberProfileList.stream()
                .map(memberProfile -> new MemberProfileListResponseDto(memberProfile, memberProfileDesiredHairstyleRepository.findByMemberProfileAndStatus(memberProfile, StatusKind.NORMAL.getId())))
                .collect(Collectors.toList());

        return makeResult(HttpStatus.OK, memberProfileListResponseDtoList);
    }
}
