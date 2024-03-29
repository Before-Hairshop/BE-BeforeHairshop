package com.beforehairshop.demo.member.service;

import com.beforehairshop.demo.ai.domain.VirtualMemberImage;
import com.beforehairshop.demo.ai.repository.VirtualMemberImageRepository;
import com.beforehairshop.demo.auth.PrincipalDetails;
import com.beforehairshop.demo.auth.handler.PrincipalDetailsUpdater;
import com.beforehairshop.demo.aws.service.AmazonS3Service;
import com.beforehairshop.demo.constant.member.profile.MatchingFlagKind;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.hairdesigner.handler.PageOffsetHandler;
import com.beforehairshop.demo.hairdesigner.repository.HairDesignerProfileRepository;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfile;
import com.beforehairshop.demo.member.domain.MemberProfileDesiredHairstyleImage;
import com.beforehairshop.demo.aws.handler.CloudFrontUrlHandler;
import com.beforehairshop.demo.member.dto.MemberDto;
import com.beforehairshop.demo.member.dto.MemberProfileDesiredHairstyleImageDto;
import com.beforehairshop.demo.member.dto.MemberProfileDto;
import com.beforehairshop.demo.member.dto.patch.MemberProfileImagePatchRequestDto;
import com.beforehairshop.demo.member.dto.patch.MemberProfilePatchRequestDto;
import com.beforehairshop.demo.member.dto.post.MemberProfileSaveRequestDto;
import com.beforehairshop.demo.member.dto.post.MemberSaveRequestDto;
import com.beforehairshop.demo.member.dto.response.MemberProfileDetailResponseDto;
import com.beforehairshop.demo.member.dto.response.MemberProfileImageResponseDto;
import com.beforehairshop.demo.member.repository.MemberProfileDesiredHairstyleImageRepository;
import com.beforehairshop.demo.member.repository.MemberProfileRepository;
import com.beforehairshop.demo.member.repository.MemberRepository;
import com.beforehairshop.demo.constant.member.StatusKind;
import com.beforehairshop.demo.recommend.domain.RecommendRequest;
import com.beforehairshop.demo.recommend.repository.RecommendRequestRepository;
import com.beforehairshop.demo.response.ResultDto;
import com.beforehairshop.demo.review.domain.Review;
import com.beforehairshop.demo.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.beforehairshop.demo.log.LogFormat.*;
import static com.beforehairshop.demo.response.ResultDto.makeResult;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    @Value("${slack.feedback.webhook.url}")
    private String slackWebhookUrl;

    private final CloudFrontUrlHandler cloudFrontUrlHandler;
    private final MemberRepository memberRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final MemberProfileDesiredHairstyleImageRepository memberProfileDesiredHairstyleImageRepository;

    private final HairDesignerProfileRepository hairDesignerProfileRepository;

    private final RecommendRequestRepository recommendRequestRepository;
    private final ReviewRepository reviewRepository;
    private final VirtualMemberImageRepository virtualMemberImageRepository;


    @Transactional
    public ResponseEntity<ResultDto> saveMemberProfile(Member member, MemberProfileSaveRequestDto memberProfileSaveRequestDto) {
        if (member == null) {
            log.error(makeLog("error", LocalDate.now(ZoneId.of("Asia/Seoul")), "fail"
                    , 504, "/api/v1/members/profiles", "POST", "세션 만료"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }

        // 이미 존재한다면, bad request 처리해준다.
        if (memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null) != null) {
            log.error(makeLog("error", LocalDate.now(ZoneId.of("Asia/Seoul")), "fail"
                    , 409, "/api/v1/members/profiles", "POST", "해당 유저는 이미 프로필이 존재함 (member_id : " + member.getId() + ")"));
            return makeResult(HttpStatus.CONFLICT, "해당 유저의 프로필은 이미 존재합니다. 유저 당 하나의 프로필만 존재할 수 있습니다.");
        }
        if (!saveDtoIsValid(memberProfileSaveRequestDto)) {
            log.error(makeLog("error", LocalDate.now(ZoneId.of("Asia/Seoul")), "fail"
                    , 204, "/api/v1/members/profiles", "POST", "해당 유저는 저장에 필요한 정보를 모두 입력하지 않았다. (member_id : " + member.getId() + ")"));
            return makeResult(HttpStatus.NO_CONTENT, "필요한 정보를 전부 입력하지 않았습니다.");
        }

        Member updatedMember = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);
        if (updatedMember == null) {
            log.error(makeLog("error", LocalDate.now(ZoneId.of("Asia/Seoul")), "fail"
                    , 400, "/api/v1/members/profiles", "POST", "유효한 유저가 아니다. DB에 존재하지 않음 (member_id : " + member.getId() + ")"));
            return makeResult(HttpStatus.BAD_REQUEST, "이 유저는 유효한 유저가 아닙니다.");
        }

        updatedMember.setName(memberProfileSaveRequestDto.getName());

        // 닉네임 변경
        PrincipalDetailsUpdater.setAuthenticationOfSecurityContext(updatedMember, "ROLE_USER");

        MemberProfile memberProfile = memberProfileSaveRequestDto.toEntity(updatedMember, null, null, null, MatchingFlagKind.ACTIVATION_CODE.getId());

        memberProfileRepository.save(memberProfile);

        log.info(makeLog("info", LocalDate.now(ZoneId.of("Asia/Seoul")), "success"
                , 200, "/api/v1/members/profiles", "POST", "유저 프로필 저장 성공 (memberProfileId : " + memberProfile.getId() + ")"));

        return makeResult(HttpStatus.OK, new MemberProfileDto(memberProfile));
    }

    private boolean saveDtoIsValid(MemberProfileSaveRequestDto memberProfileSaveRequestDto) {
        return memberProfileSaveRequestDto.getName() != null && memberProfileSaveRequestDto.getHairCondition() != null
                && memberProfileSaveRequestDto.getHairTendency() != null && memberProfileSaveRequestDto.getPayableAmount() != null
                && memberProfileSaveRequestDto.getTreatmentDate() != null && memberProfileSaveRequestDto.getPhoneNumber() != null;
    }

    public ResponseEntity<ResultDto> findMyProfile(Member member) {
        if (member == null) {
            log.error(makeLog("error", LocalDate.now(ZoneId.of("Asia/Seoul")), "fail"
                    , 504, "/api/v1/members/profiles", "GET", "세션 만료"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }

        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null) {
            return makeResult(HttpStatus.OK, null);
        }

        List<MemberProfileDesiredHairstyleImage> desiredHairstyleImageList
                = memberProfileDesiredHairstyleImageRepository.findByMemberProfileAndStatus(memberProfile, StatusKind.NORMAL.getId());


        List<MemberProfileDesiredHairstyleImageDto> memberProfileDesiredHairstyleImageDtoList = desiredHairstyleImageList.stream()
                .map(MemberProfileDesiredHairstyleImageDto::new)
                .collect(Collectors.toList());

        log.info(makeLog("info", LocalDate.now(ZoneId.of("Asia/Seoul")), "success"
                , 200, "/api/v1/members/profiles", "GET", "프로필 조회 성공 (memberProfileId : " + memberProfile.getId() + ")"));

        return makeResult(HttpStatus.OK, new MemberProfileDetailResponseDto(new MemberProfileDto(memberProfile), memberProfileDesiredHairstyleImageDtoList));
    }

    @Transactional
    public ResponseEntity<ResultDto> patchMyProfile(Member member, MemberProfilePatchRequestDto patchDto) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members/profiles", "PATCH", "세션 만료"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }

        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null) {
            log.error(makeErrorLog(404, "/api/v1/members/profiles", "PATCH", "해당 유저는 프로필이 존재하지 않는다 (memberProfileId : " + memberProfile.getId() + ")"));
            return makeResult(HttpStatus.NOT_FOUND, "해당 유저의 프로필은 존재하지 않습니다. 먼저 만들고 난 뒤 수정해야합니다.");
        }

        Member updatedMember = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);
        if (updatedMember == null) {
            log.error(makeErrorLog(400, "/api/v1/members/profiles", "PATCH", "존재하지 않는 유저 (memberId : " + member.getId() + ")"));
            return makeResult(HttpStatus.BAD_REQUEST, "유저의 세션이 만료되었거나 유효하지 않은 유저입니다.");
        }
        if (patchDto.getName() != null) {
            memberProfile.setName(patchDto.getName());
            updatedMember.setName(patchDto.getName());
        }

        if (patchDto.getHairCondition() != null)
            memberProfile.setHairCondition(patchDto.getHairCondition());

        if (patchDto.getHairTendency() != null)
            memberProfile.setHairTendency(patchDto.getHairTendency());

        if (patchDto.getDesiredHairstyle() != null)
            memberProfile.setDesiredHairstyle(patchDto.getDesiredHairstyle());

        if (patchDto.getDesiredHairstyleDescription() != null)
            memberProfile.setDesiredHairstyleDescription(patchDto.getDesiredHairstyleDescription());

        if (patchDto.getPayableAmount() != null)
            memberProfile.setPayableAmount(patchDto.getPayableAmount());

        if (patchDto.getZipCode() != null) {
            memberProfile.setZipCode(patchDto.getZipCode());
            memberProfile.setZipAddress(patchDto.getZipAddress());
            memberProfile.setLatitude(patchDto.getLatitude());
            memberProfile.setLongitude(patchDto.getLongitude());
        }

        if (patchDto.getTreatmentDate() != null)
            memberProfile.setTreatmentDate(patchDto.getTreatmentDate());

        if (patchDto.getPhoneNumber() != null)
            memberProfile.setPhoneNumber(patchDto.getPhoneNumber());


        // 닉네임 변경
        PrincipalDetailsUpdater.setAuthenticationOfSecurityContext(updatedMember, "ROLE_USER");

        memberProfile.setMember(updatedMember);

        log.info(makeSuccessLog(200, "/api/v1/members/profiles", "PATCH", "유저 프로필 수정 성공 (memberProfileId : " + memberProfile.getId() + ")"));

        return makeResult(HttpStatus.OK, new MemberProfileDto(memberProfile));
    }

    @Transactional
    public ResponseEntity<ResultDto> findMeByDB(Member member) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members", "GET", "세션 만료"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }

        Member memberByDB = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);
        if (memberByDB == null) {
            log.error(makeErrorLog(400, "/api/v1/members", "GET", "DB에 존재하지 않는 유저"));
            return makeResult(HttpStatus.BAD_REQUEST, "DB에 존재하지 않는 유저이거나, 유효하지 않은 유저이거나, 잘못된 세션 값으로 요청했습니다.");
        }


        log.info(makeSuccessLog(200, "/api/v1/members", "GET", "유저 조회 성공"));

        return makeResult(HttpStatus.OK, new MemberDto(memberByDB));
    }

    @Transactional
    public ResponseEntity<ResultDto> changeToDesigner(Member member) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members/change_to_designer", "PATCH", "세션 만료"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }

        Member updatedMember = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);

        if (updatedMember == null) {
            log.error(makeErrorLog(400, "/api/v1/members/change_to_designer", "PATCH", "존재하지 않는 유저"));

            return makeResult(HttpStatus.BAD_REQUEST, "존재하지 않는 유저입니다.");
        }

        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(
                member, StatusKind.NORMAL.getId()
        ).orElse(null);

        if (memberProfile != null)
            memberProfileRepository.delete(memberProfile);
            //memberProfile.setStatus(StatusKind.DELETE.getId());

        updatedMember.setDesignerFlag(1);
        updatedMember.setRole("ROLE_DESIGNER");
        updatedMember.setImageUrl(null);

        PrincipalDetailsUpdater.setAuthenticationOfSecurityContext(updatedMember, "ROLE_DESIGNER");

        log.info(makeSuccessLog(200, "/api/v1/members/change_to_designer", "PATCH", "디자이너로 변경 성공 (memberId : " + member.getId() + ")"));

        return makeResult(HttpStatus.OK, new MemberDto(updatedMember));
    }

    @Transactional
    public ResponseEntity<ResultDto> changeToUser(Member member) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members/change_to_user", "PATCH", "세션 만료"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }

        Member updatedMember = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);

        if (updatedMember == null) {
            log.error(makeErrorLog(400, "/api/v1/members/change_to_user", "PATCH", "삭제된 유저"));
            return makeResult(HttpStatus.BAD_REQUEST, "세션이 만료되었거나 삭제된 유저입니다.");
        }

        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(
                member, StatusKind.NORMAL.getId()
        ).orElse(null);

        if (hairDesignerProfile != null)
            hairDesignerProfileRepository.delete(hairDesignerProfile);

        updatedMember.setDesignerFlag(0);
        updatedMember.setRole("ROLE_USER");
        updatedMember.setImageUrl(null);

        PrincipalDetailsUpdater.setAuthenticationOfSecurityContext(updatedMember, "ROLE_USER");

        log.info(makeSuccessLog(200, "/api/v1/members/change_to_user", "PATCH", "유저로 변경 성공 (memberId : " + member.getId() + ")"));

        return makeResult(HttpStatus.OK, new MemberDto(updatedMember));
    }

    @Transactional
    public ResponseEntity<ResultDto> validation(Member member, Integer hairDesignerFlag) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members/validation", "PATCH", "세션 만료"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }

        Member updatedMember = memberRepository.findById(member.getId()).orElse(null);
        if (updatedMember == null) {
            log.error(makeErrorLog(400, "/api/v1/members/validation", "PATCH", "존재하지 않는 유저"));
            return makeResult(HttpStatus.BAD_REQUEST, "존재하지 않는 유저");
        }

        if (hairDesignerFlag != 1 && hairDesignerFlag != 0) {
            log.error(makeErrorLog(400, "/api/v1/members/validation", "PATCH", "잘못된 헤어 디자이너 플래그 값"));

            return makeResult(HttpStatus.BAD_REQUEST, "헤어 디자이너 플래그 값은 0 혹은 1입니다.");
        }

        updatedMember.setStatus(StatusKind.NORMAL.getId());
        updatedMember.setDesignerFlag(hairDesignerFlag);

        String userRole = null;
        if (hairDesignerFlag.equals(1))
            userRole = "ROLE_DESIGNER";
        else
            userRole = "ROLE_USER";

        updatedMember.setRole(userRole);

        PrincipalDetailsUpdater.setAuthenticationOfSecurityContext(updatedMember, userRole);

        log.info(makeSuccessLog(200, "/api/v1/members/validation", "PATCH", "약관 동의 + 디자이너/유저 선택 성공 (memberId : " + member.getId() + ")"));
        return makeResult(HttpStatus.OK, new MemberDto(updatedMember));
    }


    @Transactional
    public ResponseEntity<ResultDto> saveMemberProfileImage(Member member, Integer frontImageFlag, Integer sideImageFlag, Integer backImageFlag
            , Integer desiredHairstyleImageCount, AmazonS3Service amazonS3Service) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members/profiles/image","POST", "세션 만료"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }

        MemberProfile memberProfile = memberProfileRepository.findMemberAndProfileByMemberAndStatusUsingFetchJoin(member, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null) {
            log.error(makeErrorLog(400, "/api/v1/members/profiles/image","POST", "이미지 등록 전, 프로필 등록해야 함"));
            return makeResult(HttpStatus.BAD_REQUEST, "이미지 등록 전, 프로필을 등록해야 합니다");
        }

        if (frontImageFlag != 1) {
            log.error(makeErrorLog(204, "/api/v1/members/profiles/image","POST", "front image 는 무조건 들어와야 함"));
            return makeResult(HttpStatus.NO_CONTENT, "Front image 는 무조건 입력해야 합니다.");
        }

        String frontPreSignedUrl = amazonS3Service.generatePreSignedUrl(
                cloudFrontUrlHandler.getProfileOfUserS3Path(member.getId(), "front")
        );

        memberProfile.setFrontImageUrl(cloudFrontUrlHandler.getProfileOfUserImageUrl(member.getId(), "front"));

        String sidePreSignedUrl = null, backPreSignedUrl = null;
        if (sideImageFlag == 1) {
            sidePreSignedUrl = amazonS3Service.generatePreSignedUrl(cloudFrontUrlHandler.getProfileOfUserS3Path(member.getId(), "side"));
            memberProfile.setSideImageUrl(cloudFrontUrlHandler.getProfileOfUserImageUrl(member.getId(), "side"));
        }
        if (backImageFlag == 1) {
            backPreSignedUrl = amazonS3Service.generatePreSignedUrl(cloudFrontUrlHandler.getProfileOfUserS3Path(member.getId(), "back"));
            memberProfile.setBackImageUrl(cloudFrontUrlHandler.getProfileOfUserImageUrl(member.getId(), "back"));
        }

        Member updatedMember = memberProfile.getMember();

        updatedMember.setImageUrl(cloudFrontUrlHandler.getProfileOfUserImageUrl(member.getId(), "front"));
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
                    cloudFrontUrlHandler.getProfileOfUserDesiredStyleS3Path(memberProfile.getId(), imageEntity.getId())
            );

            desiredHairstyleImagePreSignedUrlList.add(preSignedUrl);

            // image Url 수정
            imageEntity.setImageUrl(cloudFrontUrlHandler.getProfileOfUserDesiredStyleImageUrl(memberProfile.getId(), imageEntity.getId()));

            memberProfile.getMemberProfileDesiredHairstyleImageSet().add(imageEntity);
        }

        log.info(makeSuccessLog(200, "/api/v1/members/profiles/image","POST", "유저 프로필 이미지 등록 성공 (memberProfileId : " + memberProfile.getId() + ")"));

        return makeResult(HttpStatus.OK, new MemberProfileImageResponseDto(frontPreSignedUrl, sidePreSignedUrl, backPreSignedUrl, desiredHairstyleImagePreSignedUrlList));
    }

    @Transactional
    public ResponseEntity<ResultDto> patchMyProfileImage(Member member, MemberProfileImagePatchRequestDto imagePatchRequestDto, AmazonS3Service amazonS3Service) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members/profiles/image", "PATCH", "세션 만료"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }

        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);

        if (memberProfile == null) {
            log.error(makeErrorLog(404, "/api/v1/members/profiles/image", "PATCH", "유저 프로필이 등록되어 있지 않다 (memberId : " + member.getId() + ")"));

            return makeResult(HttpStatus.NOT_FOUND, "해당 유저에게는 유저 프로필이 등록되어 있지 않습니다.");
        }

        // 원하는 스타일 이미지 중 삭제할 이미지 삭제

        if (imagePatchRequestDto.getDeleteDesiredImageUrlList() != null) {

            List<MemberProfileDesiredHairstyleImage> desiredHairstyleImageList = new ArrayList<>();
            for (String s : imagePatchRequestDto.getDeleteDesiredImageUrlList()) {
                MemberProfileDesiredHairstyleImage desiredHairstyleImage
                        = memberProfileDesiredHairstyleImageRepository.findByImageUrlAndStatus(s, StatusKind.NORMAL.getId()).orElse(null);

                if (desiredHairstyleImage == null) {
                    log.error(makeErrorLog(400, "/api/v1/members/profiles/image", "PATCH", "잘못된 이미지 URL 로 삭제하려 함"));
                    return makeResult(HttpStatus.BAD_REQUEST, "존재하지 않는 image url 입니다.");
                }

                desiredHairstyleImageList.add(desiredHairstyleImage);
//                memberProfileDesiredHairstyleImageRepository.delete(desiredHairstyleImage);
//                memberProfile.getMemberProfileDesiredHairstyleImageSet().remove(desiredHairstyleImage);
            }

            for (MemberProfileDesiredHairstyleImage desiredHairstyleImage : desiredHairstyleImageList) {
                memberProfileDesiredHairstyleImageRepository.delete(desiredHairstyleImage);
                memberProfile.getMemberProfileDesiredHairstyleImageSet().remove(desiredHairstyleImage);
            }
        }

        String frontPreSignedUrl = null, sidePreSignedUrl = null, backPreSignedUrl = null;

        if (imagePatchRequestDto.getFrontImageFlag() == 1) {
            frontPreSignedUrl = amazonS3Service.generatePreSignedUrl(cloudFrontUrlHandler.getProfileOfUserS3Path(member.getId(), "front"));
        }

        if (imagePatchRequestDto.getSideImageFlag() == 1) {
            sidePreSignedUrl = amazonS3Service.generatePreSignedUrl(cloudFrontUrlHandler.getProfileOfUserS3Path(member.getId(), "side"));
            memberProfile.setSideImageUrl(cloudFrontUrlHandler.getProfileOfUserImageUrl(member.getId(), "side"));
        }
        if (imagePatchRequestDto.getBackImageFlag() == 1) {
            backPreSignedUrl = amazonS3Service.generatePreSignedUrl(cloudFrontUrlHandler.getProfileOfUserS3Path(member.getId(), "back"));
            memberProfile.setBackImageUrl(cloudFrontUrlHandler.getProfileOfUserImageUrl(member.getId(), "back"));
        }

        // 추가할 이미지의 pre signed url 만들어서 리턴해준다.
        List<String> desiredHairstyleImagePreSignedUrlList = new ArrayList<>();
        for (int i = 0; i < imagePatchRequestDto.getAddDesiredHairstyleImageCount(); i++) {
            MemberProfileDesiredHairstyleImage imageEntity
                    = MemberProfileDesiredHairstyleImage.builder()
                    .memberProfile(memberProfile)
                    .imageUrl(null)
                    .status(StatusKind.NORMAL.getId())
                    .build();

            imageEntity = memberProfileDesiredHairstyleImageRepository.save(imageEntity);
            String preSignedUrl = amazonS3Service.generatePreSignedUrl(
                    cloudFrontUrlHandler.getProfileOfUserDesiredStyleS3Path(memberProfile.getId(), imageEntity.getId())
            );

            desiredHairstyleImagePreSignedUrlList.add(preSignedUrl);

            // image Url 수정
            imageEntity.setImageUrl(cloudFrontUrlHandler.getProfileOfUserDesiredStyleImageUrl(memberProfile.getId(), imageEntity.getId()));
            memberProfile.getMemberProfileDesiredHairstyleImageSet().add(imageEntity);
        }

        log.info(makeSuccessLog(200, "/api/v1/members/profiles/image", "PATCH", "유저 프로필 이미지 수정 완료 (memberProfileId : " + memberProfile.getId() + ")"));

        return makeResult(HttpStatus.OK, new MemberProfileImageResponseDto(frontPreSignedUrl, sidePreSignedUrl, backPreSignedUrl
                , desiredHairstyleImagePreSignedUrlList));
    }

    @Transactional
    public ResponseEntity<ResultDto> findManyProfileByLocation(Member designer, Integer pageNumber) {
        if (designer == null) {
            log.error(makeErrorLog(504, "/api/v1/members/profiles/list_by_location", "GET", "세션 만료"));

            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }

        Member hairDesigner = memberRepository.findByIdAndStatus(designer.getId(), StatusKind.NORMAL.getId()).orElse(null);
        if (hairDesigner == null || hairDesigner.getDesignerFlag() != 1) {
            log.error(makeErrorLog(400, "/api/v1/members/profiles/list_by_location", "GET", "저장된 유저가 아니거나, 디자이너가 아님 (memberId : " + hairDesigner.getId() + ")"));

            return makeResult(HttpStatus.BAD_REQUEST, "헤어 디자이너가 아니거나, 저장된 유저가 아닙니다.");
        }

        HairDesignerProfile hairDesignerProfile
                = hairDesignerProfileRepository.findByHairDesignerAndStatus(designer, StatusKind.NORMAL.getId()).orElse(null);

        if (hairDesignerProfile == null) {
            log.error(makeErrorLog(404, "/api/v1/members/profiles/list_by_location", "GET", "디자이너 프로필이 없음 (memberId : " + designer.getId() + ")"));

            return makeResult(HttpStatus.NOT_FOUND, "헤어 디자이너가 프로필이 없습니다.");
        }

        List<MemberProfile> memberProfileList
                = memberProfileRepository.findManyByLocationAndMatchingFlagAndStatus(hairDesignerProfile.getLatitude(), hairDesignerProfile.getLongitude()
                , new PageOffsetHandler().getOffsetByPageNumber(pageNumber), StatusKind.NORMAL.getId());

        List<MemberProfileDto> memberProfileListResponseDtoList = memberProfileList.stream()
                .map(MemberProfileDto::new)
                .collect(Collectors.toList());

        log.info(makeSuccessLog(200, "/api/v1/members/profiles/list_by_location", "GET", "디자이너 프로필 리스트 조회 성공"));

        return makeResult(HttpStatus.OK, memberProfileListResponseDtoList);
    }

    @Transactional
    public ResponseEntity<ResultDto> patchMyProfileActivateMatchingFlag(Member member) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members/profiles/activate_matching", "PATCH", "세션 만료"));

            log.error("[PATCH] /api/v1/members/profiles/activate_matching - 504 (세션 만료)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }

        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null) {
            log.error(makeErrorLog(400, "/api/v1/members/profiles/activate_matching", "PATCH", "프로필이 등록되어 있지 않았다 (memberId : " + member.getId() + ")"));
            return makeResult(HttpStatus.BAD_REQUEST, "해당 유저에게는 프로필 등록되어 있지 않음.");
        }
        memberProfile.setMatchingActivationFlag(MatchingFlagKind.ACTIVATION_CODE.getId());

        log.info(makeSuccessLog(200, "/api/v1/members/profiles/activate_matching", "PATCH", "프로필 활성화 성공 (memberProfileId : " + memberProfile.getId() + ")"));


        return makeResult(HttpStatus.OK, new MemberProfileDto(memberProfile));
    }

    @Transactional
    public ResponseEntity<ResultDto> patchMyProfileDeactivateMatchingFlag(Member member) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members/profiles/deactivate_matching", "PATCH", "세션 만료"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }
        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null) {
            log.error(makeErrorLog(400, "/api/v1/members/profiles/deactivate_matching", "PATCH", "프로필이 등록되어 있지 않음 (memberId : " + member.getId() + ")"));
            return makeResult(HttpStatus.BAD_REQUEST, "해당 유저에게는 프로필 등록되어 있지 않음.");
        }

        memberProfile.setMatchingActivationFlag(MatchingFlagKind.DEACTIVATION_CODE.getId());

        List<RecommendRequest> recommendRequestList = recommendRequestRepository.findByFromRecommendRequestProfileAndStatus(
                memberProfile, StatusKind.NORMAL.getId()
        );
        if (recommendRequestList != null) {
            recommendRequestRepository.deleteAll(recommendRequestList);
        }

        log.info(makeSuccessLog(200, "/api/v1/members/profiles/deactivate_matching", "PATCH", "프로필 비활성화 성공 (memberProfileId : " + memberProfile.getId() + ")"));

        return makeResult(HttpStatus.OK, new MemberProfileDto(memberProfile));
    }

    public ResponseEntity<ResultDto> findMeBySession(PrincipalDetails principalDetails) {
        if (principalDetails == null) {
            log.error(makeErrorLog(404, "/api/v1/members/session", "GET", "세션 정보 없음"));
            return makeResult(HttpStatus.NOT_FOUND, "세션 정보가 없다.");
        }
        Member member = principalDetails.getMember();
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members/session", "GET", "세션 만료"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }

        log.info(makeSuccessLog(200, "/api/v1/members/session", "GET", "세션 정보 리턴 성공"));
        return makeResult(HttpStatus.OK, new MemberDto(member));
    }

    @Transactional
    public ResponseEntity<ResultDto> deleteMyProfile(Member member) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members/profiles", "DELETE", "세션 만료"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }
        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null) {
            log.error(makeErrorLog(400, "/api/v1/members/profiles", "DELETE", "해당 유저에게는 프로필이 존재하지 않는다 (member_id : " + member.getId() + ")"));
            return makeResult(HttpStatus.BAD_REQUEST, "해당 유저에게는 프로필 등록되어 있지 않음.");
        }

        memberProfileRepository.delete(memberProfile);

        log.info(makeSuccessLog(200, "/api/v1/members/profiles", "DELETE", "프로필 삭제 완료 (member_id : " + member.getId() + ")"));
        return makeResult(HttpStatus.OK, "유저 프로필 삭제 완료");
    }

    public ResponseEntity<ResultDto> findMemberProfile(Member member, BigInteger memberProfileId) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members/profiles/detail", "GET", "세션 만료"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }

        MemberProfile memberProfile = memberProfileRepository.findByIdAndStatus(memberProfileId, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null) {
            log.error(makeErrorLog(404, "/api/v1/members/profiles/detail", "GET", "잘못된 프로필 ID 입니다 (memberProfileId : " + memberProfileId + ")"));
            return makeResult(HttpStatus.NOT_FOUND, "잘못된 프로필 ID 입니다.");
        }

        List<MemberProfileDesiredHairstyleImage> memberProfileDesiredHairstyleImageList
                = memberProfileDesiredHairstyleImageRepository.findByMemberProfileAndStatus(memberProfile, StatusKind.NORMAL.getId());

        if (memberProfileDesiredHairstyleImageList == null || memberProfileDesiredHairstyleImageList.size() == 0) {
            return makeResult(HttpStatus.OK, new MemberProfileDetailResponseDto(
                    new MemberProfileDto(memberProfile), null
            ));
        }

        List<MemberProfileDesiredHairstyleImageDto> imageDtoList
                = memberProfileDesiredHairstyleImageList.stream().map(MemberProfileDesiredHairstyleImageDto::new).collect(Collectors.toList());

        log.info(makeSuccessLog(200, "/api/v1/members/profiles/detail", "GET", "프로필 조회 성공 (memberProfileId : " + memberProfileId + ")"));

        return makeResult(HttpStatus.OK, new MemberProfileDetailResponseDto(
                new MemberProfileDto(memberProfile)
                , imageDtoList
        ));
    }

    @Transactional
    public ResponseEntity<ResultDto> delete(Member member, HttpServletRequest request) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members", "DELETE", "세션 만료"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }

        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);

        if (memberProfile != null)
            memberProfileRepository.delete(memberProfile);

        if (hairDesignerProfile != null)
            hairDesignerProfileRepository.delete(hairDesignerProfile);

        List<Review> reviewList = reviewRepository.findByReviewerAndStatus(member, StatusKind.NORMAL.getId());
        reviewRepository.deleteAll(reviewList);

        List<VirtualMemberImage> virtualMemberImageList = virtualMemberImageRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId());
        virtualMemberImageRepository.deleteAll(virtualMemberImageList);

        memberRepository.delete(member);

        HttpSession session = request.getSession(false);
        session.invalidate();
        // 시큐리티 인증정보 없애기
        SecurityContextHolder.getContext().setAuthentication(null);

        log.info(makeSuccessLog(200, "/api/v1/members", "DELETE", "유저 삭제 완료 (member_id : " + member.getId() + ")"));

        return makeResult(HttpStatus.OK, "삭제 완료");
    }

    public ResponseEntity<ResultDto> createUserFeedback(BigInteger memberId, String feedback) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> request = new HashMap<>();
        request.put("username", "유저 피드백"); //slack bot name
        request.put("text", "피드백 보낸 유저의 ID(PK) : " + memberId + "\nContent : " + feedback); //전송할 메세지

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request);

        restTemplate.exchange(slackWebhookUrl, HttpMethod.POST, entity, String.class);

        return makeResult(HttpStatus.OK, "피드백 전송 완료");
    }
}
