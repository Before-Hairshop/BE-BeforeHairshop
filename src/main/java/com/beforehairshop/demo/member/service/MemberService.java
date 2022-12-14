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
                    , 504, "/api/v1/members/profiles", "POST", "?????? ??????"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }

        // ?????? ???????????????, bad request ???????????????.
        if (memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null) != null) {
            log.error(makeLog("error", LocalDate.now(ZoneId.of("Asia/Seoul")), "fail"
                    , 409, "/api/v1/members/profiles", "POST", "?????? ????????? ?????? ???????????? ????????? (member_id : " + member.getId() + ")"));
            return makeResult(HttpStatus.CONFLICT, "?????? ????????? ???????????? ?????? ???????????????. ?????? ??? ????????? ???????????? ????????? ??? ????????????.");
        }
        if (!saveDtoIsValid(memberProfileSaveRequestDto)) {
            log.error(makeLog("error", LocalDate.now(ZoneId.of("Asia/Seoul")), "fail"
                    , 204, "/api/v1/members/profiles", "POST", "?????? ????????? ????????? ????????? ????????? ?????? ???????????? ?????????. (member_id : " + member.getId() + ")"));
            return makeResult(HttpStatus.NO_CONTENT, "????????? ????????? ?????? ???????????? ???????????????.");
        }

        Member updatedMember = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);
        if (updatedMember == null) {
            log.error(makeLog("error", LocalDate.now(ZoneId.of("Asia/Seoul")), "fail"
                    , 400, "/api/v1/members/profiles", "POST", "????????? ????????? ?????????. DB??? ???????????? ?????? (member_id : " + member.getId() + ")"));
            return makeResult(HttpStatus.BAD_REQUEST, "??? ????????? ????????? ????????? ????????????.");
        }

        updatedMember.setName(memberProfileSaveRequestDto.getName());

        // ????????? ??????
        PrincipalDetailsUpdater.setAuthenticationOfSecurityContext(updatedMember, "ROLE_USER");

        MemberProfile memberProfile = memberProfileSaveRequestDto.toEntity(updatedMember, null, null, null, MatchingFlagKind.ACTIVATION_CODE.getId());

        memberProfileRepository.save(memberProfile);

        log.info(makeLog("info", LocalDate.now(ZoneId.of("Asia/Seoul")), "success"
                , 200, "/api/v1/members/profiles", "POST", "?????? ????????? ?????? ?????? (memberProfileId : " + memberProfile.getId() + ")"));

        return makeResult(HttpStatus.OK, new MemberProfileDto(memberProfile));
    }

    private boolean saveDtoIsValid(MemberProfileSaveRequestDto memberProfileSaveRequestDto) {
        return memberProfileSaveRequestDto.getName() != null && memberProfileSaveRequestDto.getHairCondition() != null
                && memberProfileSaveRequestDto.getHairTendency() != null && memberProfileSaveRequestDto.getPayableAmount() != null
                && memberProfileSaveRequestDto.getTreatmentDate() != null && memberProfileSaveRequestDto.getPhoneNumber() != null;
    }

    @Transactional
    public ResponseEntity<ResultDto> findMyProfile(Member member) {
        if (member == null) {
            log.error(makeLog("error", LocalDate.now(ZoneId.of("Asia/Seoul")), "fail"
                    , 504, "/api/v1/members/profiles", "GET", "?????? ??????"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
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
                , 200, "/api/v1/members/profiles", "GET", "????????? ?????? ?????? (memberProfileId : " + memberProfile.getId() + ")"));

        return makeResult(HttpStatus.OK, new MemberProfileDetailResponseDto(new MemberProfileDto(memberProfile), memberProfileDesiredHairstyleImageDtoList));
    }

    @Transactional
    public ResponseEntity<ResultDto> patchMyProfile(Member member, MemberProfilePatchRequestDto patchDto) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members/profiles", "PATCH", "?????? ??????"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }

        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null) {
            log.error(makeErrorLog(404, "/api/v1/members/profiles", "PATCH", "?????? ????????? ???????????? ???????????? ????????? (memberProfileId : " + memberProfile.getId() + ")"));
            return makeResult(HttpStatus.NOT_FOUND, "?????? ????????? ???????????? ???????????? ????????????. ?????? ????????? ??? ??? ?????????????????????.");
        }

        Member updatedMember = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);
        if (updatedMember == null) {
            log.error(makeErrorLog(400, "/api/v1/members/profiles", "PATCH", "???????????? ?????? ?????? (memberId : " + member.getId() + ")"));
            return makeResult(HttpStatus.BAD_REQUEST, "????????? ????????? ?????????????????? ???????????? ?????? ???????????????.");
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


        // ????????? ??????
        PrincipalDetailsUpdater.setAuthenticationOfSecurityContext(updatedMember, "ROLE_USER");

        memberProfile.setMember(updatedMember);

        log.info(makeSuccessLog(200, "/api/v1/members/profiles", "PATCH", "?????? ????????? ?????? ?????? (memberProfileId : " + memberProfile.getId() + ")"));

        return makeResult(HttpStatus.OK, new MemberProfileDto(memberProfile));
    }

    @Transactional
    public ResponseEntity<ResultDto> findMeByDB(Member member) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members", "GET", "?????? ??????"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }

        Member memberByDB = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);
        if (memberByDB == null) {
            log.error(makeErrorLog(400, "/api/v1/members", "GET", "DB??? ???????????? ?????? ??????"));
            return makeResult(HttpStatus.BAD_REQUEST, "DB??? ???????????? ?????? ???????????????, ???????????? ?????? ???????????????, ????????? ?????? ????????? ??????????????????.");
        }


        log.info(makeSuccessLog(200, "/api/v1/members", "GET", "?????? ?????? ??????"));

        return makeResult(HttpStatus.OK, new MemberDto(memberByDB));
    }

    @Transactional
    public ResponseEntity<ResultDto> changeToDesigner(Member member) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members/change_to_designer", "PATCH", "?????? ??????"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }

        Member updatedMember = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);

        if (updatedMember == null) {
            log.error(makeErrorLog(400, "/api/v1/members/change_to_designer", "PATCH", "???????????? ?????? ??????"));

            return makeResult(HttpStatus.BAD_REQUEST, "???????????? ?????? ???????????????.");
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

        log.info(makeSuccessLog(200, "/api/v1/members/change_to_designer", "PATCH", "??????????????? ?????? ?????? (memberId : " + member.getId() + ")"));

        return makeResult(HttpStatus.OK, new MemberDto(updatedMember));
    }

    @Transactional
    public ResponseEntity<ResultDto> changeToUser(Member member) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members/change_to_user", "PATCH", "?????? ??????"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }

        Member updatedMember = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);

        if (updatedMember == null) {
            log.error(makeErrorLog(400, "/api/v1/members/change_to_user", "PATCH", "????????? ??????"));
            return makeResult(HttpStatus.BAD_REQUEST, "????????? ?????????????????? ????????? ???????????????.");
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

        log.info(makeSuccessLog(200, "/api/v1/members/change_to_user", "PATCH", "????????? ?????? ?????? (memberId : " + member.getId() + ")"));

        return makeResult(HttpStatus.OK, new MemberDto(updatedMember));
    }

    @Transactional
    public ResponseEntity<ResultDto> validation(Member member, Integer hairDesignerFlag) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members/validation", "PATCH", "?????? ??????"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }

        Member updatedMember = memberRepository.findById(member.getId()).orElse(null);
        if (updatedMember == null) {
            log.error(makeErrorLog(400, "/api/v1/members/validation", "PATCH", "???????????? ?????? ??????"));
            return makeResult(HttpStatus.BAD_REQUEST, "???????????? ?????? ??????");
        }

        if (hairDesignerFlag != 1 && hairDesignerFlag != 0) {
            log.error(makeErrorLog(400, "/api/v1/members/validation", "PATCH", "????????? ?????? ???????????? ????????? ???"));

            return makeResult(HttpStatus.BAD_REQUEST, "?????? ???????????? ????????? ?????? 0 ?????? 1?????????.");
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

        log.info(makeSuccessLog(200, "/api/v1/members/validation", "PATCH", "?????? ?????? + ????????????/?????? ?????? ?????? (memberId : " + member.getId() + ")"));
        return makeResult(HttpStatus.OK, new MemberDto(updatedMember));
    }


    @Transactional
    public ResponseEntity<ResultDto> saveMemberProfileImage(Member member, Integer frontImageFlag, Integer sideImageFlag, Integer backImageFlag
            , Integer desiredHairstyleImageCount, AmazonS3Service amazonS3Service) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members/profiles/image","POST", "?????? ??????"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }

        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null) {
            log.error(makeErrorLog(400, "/api/v1/members/profiles/image","POST", "????????? ?????? ???, ????????? ???????????? ???"));
            return makeResult(HttpStatus.BAD_REQUEST, "????????? ?????? ???, ???????????? ???????????? ?????????");
        }

        if (frontImageFlag != 1) {
            log.error(makeErrorLog(204, "/api/v1/members/profiles/image","POST", "front image ??? ????????? ???????????? ???"));
            return makeResult(HttpStatus.NO_CONTENT, "Front image ??? ????????? ???????????? ?????????.");
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

        Member updatedMember = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);

        updatedMember.setImageUrl(cloudFrontUrlHandler.getProfileOfUserImageUrl(member.getId(), "front"));
        PrincipalDetailsUpdater.setAuthenticationOfSecurityContext(updatedMember, "ROLE_USER");


        // ????????? ????????? ????????? ??????
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

            // image Url ??????
            imageEntity.setImageUrl(cloudFrontUrlHandler.getProfileOfUserDesiredStyleImageUrl(memberProfile.getId(), imageEntity.getId()));

            memberProfile.getMemberProfileDesiredHairstyleImageSet().add(imageEntity);
        }

        log.info(makeSuccessLog(200, "/api/v1/members/profiles/image","POST", "?????? ????????? ????????? ?????? ?????? (memberProfileId : " + memberProfile.getId() + ")"));

        return makeResult(HttpStatus.OK, new MemberProfileImageResponseDto(frontPreSignedUrl, sidePreSignedUrl, backPreSignedUrl, desiredHairstyleImagePreSignedUrlList));
    }

    @Transactional
    public ResponseEntity<ResultDto> patchMyProfileImage(Member member, MemberProfileImagePatchRequestDto imagePatchRequestDto, AmazonS3Service amazonS3Service) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members/profiles/image", "PATCH", "?????? ??????"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }

        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);

        if (memberProfile == null) {
            log.error(makeErrorLog(404, "/api/v1/members/profiles/image", "PATCH", "?????? ???????????? ???????????? ?????? ?????? (memberId : " + member.getId() + ")"));

            return makeResult(HttpStatus.NOT_FOUND, "?????? ??????????????? ?????? ???????????? ???????????? ?????? ????????????.");
        }

        // ????????? ????????? ????????? ??? ????????? ????????? ??????

        if (imagePatchRequestDto.getDeleteDesiredImageUrlList() != null) {

            List<MemberProfileDesiredHairstyleImage> desiredHairstyleImageList = new ArrayList<>();
            for (String s : imagePatchRequestDto.getDeleteDesiredImageUrlList()) {
                MemberProfileDesiredHairstyleImage desiredHairstyleImage
                        = memberProfileDesiredHairstyleImageRepository.findByImageUrlAndStatus(s, StatusKind.NORMAL.getId()).orElse(null);

                if (desiredHairstyleImage == null) {
                    log.error(makeErrorLog(400, "/api/v1/members/profiles/image", "PATCH", "????????? ????????? URL ??? ???????????? ???"));
                    return makeResult(HttpStatus.BAD_REQUEST, "???????????? ?????? image url ?????????.");
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

        // ????????? ???????????? pre signed url ???????????? ???????????????.
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

            // image Url ??????
            imageEntity.setImageUrl(cloudFrontUrlHandler.getProfileOfUserDesiredStyleImageUrl(memberProfile.getId(), imageEntity.getId()));
            memberProfile.getMemberProfileDesiredHairstyleImageSet().add(imageEntity);
        }

        log.info(makeSuccessLog(200, "/api/v1/members/profiles/image", "PATCH", "?????? ????????? ????????? ?????? ?????? (memberProfileId : " + memberProfile.getId() + ")"));

        return makeResult(HttpStatus.OK, new MemberProfileImageResponseDto(frontPreSignedUrl, sidePreSignedUrl, backPreSignedUrl
                , desiredHairstyleImagePreSignedUrlList));
    }

    @Transactional
    public ResponseEntity<ResultDto> findManyProfileByLocation(Member designer, Integer pageNumber) {
        if (designer == null) {
            log.error(makeErrorLog(504, "/api/v1/members/profiles/list_by_location", "GET", "?????? ??????"));

            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }

        Member hairDesigner = memberRepository.findByIdAndStatus(designer.getId(), StatusKind.NORMAL.getId()).orElse(null);
        if (hairDesigner == null || hairDesigner.getDesignerFlag() != 1) {
            log.error(makeErrorLog(400, "/api/v1/members/profiles/list_by_location", "GET", "????????? ????????? ????????????, ??????????????? ?????? (memberId : " + hairDesigner.getId() + ")"));

            return makeResult(HttpStatus.BAD_REQUEST, "?????? ??????????????? ????????????, ????????? ????????? ????????????.");
        }

        HairDesignerProfile hairDesignerProfile
                = hairDesignerProfileRepository.findByHairDesignerAndStatus(designer, StatusKind.NORMAL.getId()).orElse(null);

        if (hairDesignerProfile == null) {
            log.error(makeErrorLog(404, "/api/v1/members/profiles/list_by_location", "GET", "???????????? ???????????? ?????? (memberId : " + designer.getId() + ")"));

            return makeResult(HttpStatus.NOT_FOUND, "?????? ??????????????? ???????????? ????????????.");
        }

        List<MemberProfile> memberProfileList
                = memberProfileRepository.findManyByLocationAndMatchingFlagAndStatus(hairDesignerProfile.getLatitude(), hairDesignerProfile.getLongitude()
                , new PageOffsetHandler().getOffsetByPageNumber(pageNumber), StatusKind.NORMAL.getId());

        List<MemberProfileDto> memberProfileListResponseDtoList = memberProfileList.stream()
                .map(MemberProfileDto::new)
                .collect(Collectors.toList());

        log.info(makeSuccessLog(200, "/api/v1/members/profiles/list_by_location", "GET", "???????????? ????????? ????????? ?????? ??????"));

        return makeResult(HttpStatus.OK, memberProfileListResponseDtoList);
    }

    @Transactional
    public ResponseEntity<ResultDto> patchMyProfileActivateMatchingFlag(Member member) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members/profiles/activate_matching", "PATCH", "?????? ??????"));

            log.error("[PATCH] /api/v1/members/profiles/activate_matching - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }

        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null) {
            log.error(makeErrorLog(400, "/api/v1/members/profiles/activate_matching", "PATCH", "???????????? ???????????? ?????? ????????? (memberId : " + member.getId() + ")"));
            return makeResult(HttpStatus.BAD_REQUEST, "?????? ??????????????? ????????? ???????????? ?????? ??????.");
        }
        memberProfile.setMatchingActivationFlag(MatchingFlagKind.ACTIVATION_CODE.getId());

        log.info(makeSuccessLog(200, "/api/v1/members/profiles/activate_matching", "PATCH", "????????? ????????? ?????? (memberProfileId : " + memberProfile.getId() + ")"));


        return makeResult(HttpStatus.OK, new MemberProfileDto(memberProfile));
    }

    @Transactional
    public ResponseEntity<ResultDto> patchMyProfileDeactivateMatchingFlag(Member member) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members/profiles/deactivate_matching", "PATCH", "?????? ??????"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null) {
            log.error(makeErrorLog(400, "/api/v1/members/profiles/deactivate_matching", "PATCH", "???????????? ???????????? ?????? ?????? (memberId : " + member.getId() + ")"));
            return makeResult(HttpStatus.BAD_REQUEST, "?????? ??????????????? ????????? ???????????? ?????? ??????.");
        }

        memberProfile.setMatchingActivationFlag(MatchingFlagKind.DEACTIVATION_CODE.getId());

        List<RecommendRequest> recommendRequestList = recommendRequestRepository.findByFromRecommendRequestProfileAndStatus(
                memberProfile, StatusKind.NORMAL.getId()
        );
        if (recommendRequestList != null) {
            recommendRequestRepository.deleteAll(recommendRequestList);
        }

        log.info(makeSuccessLog(200, "/api/v1/members/profiles/deactivate_matching", "PATCH", "????????? ???????????? ?????? (memberProfileId : " + memberProfile.getId() + ")"));

        return makeResult(HttpStatus.OK, new MemberProfileDto(memberProfile));
    }

    @Transactional
    public ResponseEntity<ResultDto> findMeBySession(PrincipalDetails principalDetails) {
        if (principalDetails == null) {
            log.error(makeErrorLog(404, "/api/v1/members/session", "GET", "?????? ?????? ??????"));
            return makeResult(HttpStatus.NOT_FOUND, "?????? ????????? ??????.");
        }
        Member member = principalDetails.getMember();
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members/session", "GET", "?????? ??????"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }

        log.info(makeSuccessLog(200, "/api/v1/members/session", "GET", "?????? ?????? ?????? ??????"));
        return makeResult(HttpStatus.OK, new MemberDto(member));
    }

    @Transactional
    public ResponseEntity<ResultDto> deleteMyProfile(Member member) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members/profiles", "DELETE", "?????? ??????"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null) {
            log.error(makeErrorLog(400, "/api/v1/members/profiles", "DELETE", "?????? ??????????????? ???????????? ???????????? ????????? (member_id : " + member.getId() + ")"));
            return makeResult(HttpStatus.BAD_REQUEST, "?????? ??????????????? ????????? ???????????? ?????? ??????.");
        }

        memberProfileRepository.delete(memberProfile);

        log.info(makeSuccessLog(200, "/api/v1/members/profiles", "DELETE", "????????? ?????? ?????? (member_id : " + member.getId() + ")"));
        return makeResult(HttpStatus.OK, "?????? ????????? ?????? ??????");
    }

    @Transactional
    public ResponseEntity<ResultDto> findMemberProfile(Member member, BigInteger memberProfileId) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members/profiles/detail", "GET", "?????? ??????"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }

        MemberProfile memberProfile = memberProfileRepository.findByIdAndStatus(memberProfileId, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null) {
            log.error(makeErrorLog(404, "/api/v1/members/profiles/detail", "GET", "????????? ????????? ID ????????? (memberProfileId : " + memberProfileId + ")"));
            return makeResult(HttpStatus.NOT_FOUND, "????????? ????????? ID ?????????.");
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

        log.info(makeSuccessLog(200, "/api/v1/members/profiles/detail", "GET", "????????? ?????? ?????? (memberProfileId : " + memberProfileId + ")"));

        return makeResult(HttpStatus.OK, new MemberProfileDetailResponseDto(
                new MemberProfileDto(memberProfile)
                , imageDtoList
        ));
    }

    @Transactional
    public ResponseEntity<ResultDto> delete(Member member, HttpServletRequest request) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/members", "DELETE", "?????? ??????"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
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
        // ???????????? ???????????? ?????????
        SecurityContextHolder.getContext().setAuthentication(null);

        log.info(makeSuccessLog(200, "/api/v1/members", "DELETE", "?????? ?????? ?????? (member_id : " + member.getId() + ")"));

        return makeResult(HttpStatus.OK, "?????? ??????");
    }

    public ResponseEntity<ResultDto> createUserFeedback(BigInteger memberId, String feedback) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> request = new HashMap<>();
        request.put("username", "?????? ?????????"); //slack bot name
        request.put("text", "????????? ?????? ????????? ID(PK) : " + memberId + "\nContent : " + feedback); //????????? ?????????

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request);

        restTemplate.exchange(slackWebhookUrl, HttpMethod.POST, entity, String.class);

        return makeResult(HttpStatus.OK, "????????? ?????? ??????");
    }
}
