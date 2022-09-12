package com.beforehairshop.demo.member.service;

import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.aws.S3Uploader;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfile;
import com.beforehairshop.demo.member.dto.*;
import com.beforehairshop.demo.member.repository.MemberProfileRepository;
import com.beforehairshop.demo.member.repository.MemberRepository;
import com.beforehairshop.demo.member.domain.StatusKind;
import com.beforehairshop.demo.response.ResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;

import static com.beforehairshop.demo.response.ResultDto.makeResult;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberProfileRepository memberProfileRepository;

    @Transactional
    public BigInteger save(MemberSaveRequestDto requestDto) {
        if (memberRepository.findOneByEmailAndStatusIsLessThan(requestDto.getEmail(), StatusKind.DELETE.getId()).orElse(null) != null) {
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
    public ResponseEntity<ResultDto> saveMemberProfile(Member member, MemberProfileSaveRequestDto memberProfileSaveRequestDto, S3Uploader s3Uploader) throws IOException {
        // 이미 존재한다면, bad request 처리해준다.
        if (memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null) != null)
            return makeResult(HttpStatus.BAD_REQUEST, "해당 유저의 프로필은 이미 존재합니다. 유저 당 하나의 프로필만 존재할 수 있습니다.");



        String frontImageUrl = s3Uploader.upload(memberProfileSaveRequestDto.getFrontImage(), member.getId().toString() + "/profile/front-image.jpg");

        // member 의 image url 변경해준다.
        member.setImageUrl(frontImageUrl);

        String sideImageUrl = null, backImageUrl = null;

        if (memberProfileSaveRequestDto.getSideImage() != null)
            sideImageUrl = s3Uploader.upload(memberProfileSaveRequestDto.getSideImage(), member.getId().toString() + "/profile/side-image.jpg");

        if (memberProfileSaveRequestDto.getBackImage() != null)
            backImageUrl = s3Uploader.upload(memberProfileSaveRequestDto.getBackImage(), member.getId().toString() + "/profile/back-image.jpg");

        MemberProfile memberProfile = memberProfileSaveRequestDto.toEntity(member, frontImageUrl, sideImageUrl, backImageUrl);
        memberProfileRepository.save(memberProfile);

        return makeResult(HttpStatus.OK, memberProfile);
    }

    @Transactional
    public ResponseEntity<ResultDto> findMyProfile(Member member) {
        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null) {
            return makeResult(HttpStatus.BAD_REQUEST, "해당 유저의 프로필은 존재하지 않습니다.");
        }
        return makeResult(HttpStatus.OK, memberProfile);
    }

    @Transactional
    public ResponseEntity<ResultDto> patchMyProfile(Member member, MemberProfilePatchRequestDto patchDto, S3Uploader s3Uploader) throws IOException {
        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null) {
            return makeResult(HttpStatus.BAD_REQUEST, "해당 유저의 프로필은 존재하지 않습니다. 먼저 만들고 난 뒤 수정해야합니다.");
        }

        if (patchDto.getHairCondition() != null)
            memberProfile.setHairCondition(patchDto.getHairCondition());

        if (patchDto.getHairTendency() != null)
            memberProfile.setHairTendency(patchDto.getHairTendency());

        if (patchDto.getDesiredHairstyle() != null)
            memberProfile.setDesiredHairstyle(patchDto.getDesiredHairstyle());

        if (patchDto.getDesiredHairstyleDescription() != null)
            memberProfile.setDesiredHairstyleDescription(memberProfile.getDesiredHairstyleDescription());


        if (patchDto.getFrontImage() != null)
            memberProfile.setFrontImageUrl(s3Uploader.upload(patchDto.getFrontImage(), member.getId().toString() + "/profile/front-image.jpg"));

        if (patchDto.getSideImage() != null)
            memberProfile.setSideImageUrl(s3Uploader.upload(patchDto.getSideImage(), member.getId().toString() + "/profile/side-image.jpg"));

        if (patchDto.getBackImage() != null)
            memberProfile.setBackImageUrl(s3Uploader.upload(patchDto.getBackImage(), member.getId().toString() + "/profile/back-image.jpg"));


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


        return makeResult(HttpStatus.OK, memberProfile);
    }

//    public ResponseEntity<ResultDto> saveMemberProfileImages(Member member, MemberProfileImageSaveRequestDto memberProfileImageSaveRequestDto, S3Uploader s3Uploader) throws IOException {
//        String frontImageUrl = s3Uploader.upload(memberProfileImageSaveRequestDto.getFrontImage(), member.getId().toString() + "/profile/front-image.jpg");
//        String sideImageUrl = null, backImageUrl = null;
//
//        if (memberProfileImageSaveRequestDto.getSideImage() != null)
//            sideImageUrl = s3Uploader.upload(memberProfileImageSaveRequestDto.getSideImage(), member.getId().toString() + "/profile/side-image.jpg");
//
//        if (memberProfileImageSaveRequestDto.getBackImage() != null)
//            backImageUrl = s3Uploader.upload(memberProfileImageSaveRequestDto.getBackImage(), member.getId().toString() + "/profile/back-image.jpg");
//
//        return makeResult(HttpStatus.OK, new MemberProfileImageSaveResponseDto(frontImageUrl, sideImageUrl, backImageUrl));
//    }
}
