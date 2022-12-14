package com.beforehairshop.demo.hairdesigner.service;

import com.beforehairshop.demo.auth.handler.PrincipalDetailsUpdater;
import com.beforehairshop.demo.aws.handler.CloudFrontUrlHandler;
import com.beforehairshop.demo.aws.service.AmazonS3Service;
import com.beforehairshop.demo.constant.member.StatusKind;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerHashtag;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerPrice;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerWorkingDay;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerHashtagDto;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerPriceDto;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerProfileDto;
import com.beforehairshop.demo.hairdesigner.dto.HairDesignerWorkingDayDto;
import com.beforehairshop.demo.hairdesigner.dto.patch.HairDesignerHashtagPatchRequestDto;
import com.beforehairshop.demo.hairdesigner.dto.patch.HairDesignerPricePatchRequestDto;
import com.beforehairshop.demo.hairdesigner.dto.patch.HairDesignerWorkingDayPatchRequestDto;
import com.beforehairshop.demo.hairdesigner.dto.post.HairDesignerHashtagSaveRequestDto;
import com.beforehairshop.demo.hairdesigner.dto.post.HairDesignerPriceSaveRequestDto;
import com.beforehairshop.demo.hairdesigner.dto.post.HairDesignerWorkingDaySaveRequestDto;
import com.beforehairshop.demo.hairdesigner.dto.response.HairDesignerDetailGetResponseDto;
import com.beforehairshop.demo.hairdesigner.dto.patch.HairDesignerProfilePatchRequestDto;
import com.beforehairshop.demo.hairdesigner.dto.post.HairDesignerProfileSaveRequestDto;
import com.beforehairshop.demo.hairdesigner.dto.response.HairDesignerProfileAndDistanceAndHashtagDto;
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
import com.beforehairshop.demo.review.repository.ReviewRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.beforehairshop.demo.log.LogFormat.makeErrorLog;
import static com.beforehairshop.demo.response.ResultDto.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class HairDesignerService {

    private final CloudFrontUrlHandler cloudFrontUrlHandler;
    private final HairDesignerProfileRepository hairDesignerProfileRepository;
    private final HairDesignerWorkingDayRepository hairDesignerWorkingDayRepository;
    private final HairDesignerPriceRepository hairDesignerPriceRepository;
    private final HairDesignerHashtagRepository hairDesignerHashtagRepository;
    private final MemberRepository memberRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public ResponseEntity<ResultDto> save(Member member, HairDesignerProfileSaveRequestDto hairDesignerProfileSaveRequestDto) {
        if (member == null) {
            log.error("[POST] /api/v1/hair_designers - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        Member hairDesigner = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);
        if (hairDesigner == null) {
            log.error("[POST] /api/v1/hair_designers - 404 (?????????????????? ???????????? ?????? ??????)");
            return makeResult(HttpStatus.NOT_FOUND, "?????????????????? ???????????? ?????? ???????????????.");
        }

        if (hairDesigner.getDesignerFlag() == 0) {
            log.error("[POST] /api/v1/hair_designers - 400 (?????? ????????? ?????? ??????. ?????? ?????? ?????? ?????????)");
            return makeResult(HttpStatus.BAD_REQUEST, "??? ????????? ?????? ???????????????. ?????? ????????? ?????? ???????????? ????????????.");
        }

        HairDesignerProfile existedHairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(hairDesigner, StatusKind.NORMAL.getId()).orElse(null);
        if (existedHairDesignerProfile != null) {
            log.error("[POST] /api/v1/hair_designers - 409 (?????? ???????????? ?????????)");
            return makeResult(HttpStatus.CONFLICT, "?????? ?????? ???????????? ???????????? ???????????????.");
        }
        hairDesigner.setName(hairDesignerProfileSaveRequestDto.getName());

        HairDesignerProfile hairDesignerProfile = new HairDesignerProfile(hairDesigner, hairDesignerProfileSaveRequestDto, StatusKind.NORMAL.getId());

        if (hairDesignerProfileSaveRequestDto.getHashtagList() != null) {
            for (HairDesignerHashtagSaveRequestDto hashtagSaveRequestDto : hairDesignerProfileSaveRequestDto.getHashtagList()) {
                HairDesignerHashtag hashtag = new HairDesignerHashtag(hairDesignerProfile, hashtagSaveRequestDto.getTag(), StatusKind.NORMAL.getId());
                hairDesignerProfile.addHashtag(hashtag);
            }
        }

        if (hairDesignerProfileSaveRequestDto.getPriceList() != null) {
            for (HairDesignerPriceSaveRequestDto priceSaveRequestDto : hairDesignerProfileSaveRequestDto.getPriceList()) {
                HairDesignerPrice hairDesignerPrice = new HairDesignerPrice(hairDesignerProfile
                        , priceSaveRequestDto.getHairCategory()
                        , priceSaveRequestDto.getHairStyleName()
                        , priceSaveRequestDto.getPrice()
                        , StatusKind.NORMAL.getId());
                hairDesignerProfile.addPrice(hairDesignerPrice);
            }
        }

        if (hairDesignerProfileSaveRequestDto.getWorkingDayList() != null) {
            for (HairDesignerWorkingDaySaveRequestDto workingDaySaveRequestDto : hairDesignerProfileSaveRequestDto.getWorkingDayList()) {
                HairDesignerWorkingDay hairDesignerWorkingDay = new HairDesignerWorkingDay(hairDesignerProfile
                        , workingDaySaveRequestDto.getWorkingDay()
                        , workingDaySaveRequestDto.getStartTime()
                        , workingDaySaveRequestDto.getEndTime()
                        , StatusKind.NORMAL.getId());
                hairDesignerProfile.addWorkingDay(hairDesignerWorkingDay);
            }
        }

        hairDesignerProfileRepository.save(hairDesignerProfile);


        // ?????? ??????()
        PrincipalDetailsUpdater.setAuthenticationOfSecurityContext(hairDesigner, "ROLE_DESIGNER");

        return makeResult(HttpStatus.OK, new HairDesignerProfileDto(hairDesignerProfile));
    }

    @Transactional
    public ResponseEntity<ResultDto> findOne(Member member, BigInteger hairDesignerId) {
        if (member == null) {
            log.error("[GET] /api/v1/hair_designers/{id} - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        Member designer = memberRepository.findByIdAndStatus(hairDesignerId, StatusKind.NORMAL.getId()).orElse(null);
        if (designer == null || designer.getDesignerFlag() != 1) {
            log.error("[GET] /api/v1/hair_designers/{id} - 400 (?????? ID??? ????????? ??????????????? ??????)");
            return makeResult(HttpStatus.BAD_REQUEST, "?????? ID??? ????????? ??????????????? ??????.");
        }

        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(designer, StatusKind.NORMAL.getId()).orElse(null);
        if (hairDesignerProfile == null) {
            log.error("[GET] /api/v1/hair_designers/{id} - 404 (?????? ??????????????? ???????????? ??????.)");
            return makeResult(HttpStatus.NOT_FOUND, "?????? ??????????????? ???????????? ????????????.");
        }

        /**
         * ??????, ?????? ?????? ???????????? ??????
         */
        Float averageStarRating = reviewRepository.calculateByHairDesignerProfileIdAndStatus(hairDesignerProfile.getId(), StatusKind.NORMAL.getId());

        List<HairDesignerHashtagDto> hashtagDtoList = hairDesignerProfile.getHairDesignerHashtagSet().stream().map(HairDesignerHashtagDto::new).collect(Collectors.toList());
        List<HairDesignerWorkingDayDto> workingDayDtoList = hairDesignerProfile.getHairDesignerWorkingDaySet().stream().map(HairDesignerWorkingDayDto::new).collect(Collectors.toList());
        List<HairDesignerPriceDto> priceDtoList = hairDesignerProfile.getHairDesignerPriceSet().stream().map(HairDesignerPriceDto::new).collect(Collectors.toList());
        return makeResult(HttpStatus.OK, new HairDesignerDetailGetResponseDto(new HairDesignerProfileDto(hairDesignerProfile)
                , averageStarRating
                , hashtagDtoList
                , workingDayDtoList
                , priceDtoList));
    }

    @Transactional
    public ResponseEntity<ResultDto> findManyByLocation(Member member, Integer pageNumber) {
        if (member == null) {
            log.error("[GET] /api/v1/hair_designers/list_by_location - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null && hairDesignerProfile == null) {
            log.error("[GET] /api/v1/hair_designers/list_by_location - 400 (?????? ??????????????? ???????????? ???????????? ?????? ??????.)");
            return makeResult(HttpStatus.BAD_REQUEST, "??? ????????? ????????? ????????? ???????????? ????????????.");
        }


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

        List<HairDesignerProfileAndDistanceAndHashtagDto> hairDesignerProfileAndHashtagDtoList
                = hairDesignerProfileList.stream()
                .map(hairDesignerProfile1 -> new HairDesignerProfileAndDistanceAndHashtagDto(
                        new HairDesignerProfileDto(hairDesignerProfile1),

                        calculateDistance(hairDesignerProfile1.getLatitude(), hairDesignerProfile1.getLongitude()
                                , memberProfile.getLatitude(), memberProfile.getLongitude()),

                        reviewRepository.calculateByHairDesignerProfileIdAndStatus(hairDesignerProfile1.getId(), StatusKind.NORMAL.getId()),

                        hairDesignerProfile1.getHairDesignerHashtagSet().stream()
                                .map(HairDesignerHashtagDto::new)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        return makeResult(HttpStatus.OK, hairDesignerProfileAndHashtagDtoList);

    }

//    private double calculateDistance(Float lat1, Float lon1, Float lat2, Float lon2) {
//        double dLat = Math.toRadians(lat2 - lat1);
//        double dLon = Math.toRadians(lon2 - lon1);
//
//        double a = Math.sin(dLat/2)* Math.sin(dLat/2)+ Math.cos(Math.toRadians(lat1))* Math.cos(Math.toRadians(lat2))* Math.sin(dLon/2)* Math.sin(dLon/2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
//        double d = EARTH_RADIUS * c;    // Distance in m
//        return d;
//    }

    /**
     * @return m ????????? ??????
     */
    private long calculateDistance(Float lat1, Float lon1, Float lat2, Float lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * 1609.344;

        return (Math.round(dist));
    }


    // This function converts decimal degrees to radians
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    @Transactional
    public ResponseEntity<ResultDto> patchOne(Member hairDesigner
            , HairDesignerProfilePatchRequestDto patchDto) {
        if (hairDesigner == null) {
            log.error("[PATCH] /api/v1/hair_designers - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        Member updatedMember = memberRepository.findByIdAndStatus(hairDesigner.getId(), StatusKind.NORMAL.getId()).orElse(null);

        if (updatedMember == null || updatedMember.getDesignerFlag() != 1 || !updatedMember.getRole().equals("ROLE_DESIGNER")) {
            log.error("[PATCH] /api/v1/hair_designers - 400 (?????? ????????? ???????????? ?????????, ??????????????? ?????????)");
            return makeResult(HttpStatus.BAD_REQUEST, "?????? ????????? ???????????? ?????????, ??????????????? ????????????.");
        }


        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(hairDesigner, StatusKind.NORMAL.getId()).orElse(null);
        if (hairDesignerProfile == null) {
            log.error("[PATCH] /api/v1/hair_designers - 404 (?????? ??????????????? ???????????? ??????.)");
            return makeResult(HttpStatus.NOT_FOUND, "?????? ????????? ???????????? ???????????? ????????????.");
        }
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
            hairDesignerProfile.getHairDesignerHashtagSet().clear();


            for (HairDesignerHashtagPatchRequestDto patchRequestDto : patchDto.getHashtagPatchRequestDtoList()) {
                hairDesignerProfile.getHairDesignerHashtagSet().add(patchRequestDto.toEntity(hairDesignerProfile));
            }

        }

        if (patchDto.getPricePatchRequestDtoList() != null) {
            hairDesignerProfile.getHairDesignerPriceSet().clear();

            for (HairDesignerPricePatchRequestDto patchRequestDto : patchDto.getPricePatchRequestDtoList()) {
                hairDesignerProfile.getHairDesignerPriceSet().add(patchRequestDto.toEntity(hairDesignerProfile));
            }

        }

        if (patchDto.getWorkingDayPatchRequestDtoList() != null) {
            hairDesignerProfile.getHairDesignerWorkingDaySet().clear();

            for (HairDesignerWorkingDayPatchRequestDto patchRequestDto : patchDto.getWorkingDayPatchRequestDtoList()) {
                hairDesignerProfile.getHairDesignerWorkingDaySet().add(patchRequestDto.toEntity(hairDesignerProfile));
            }

        }


        // ????????? ??????
        PrincipalDetailsUpdater.setAuthenticationOfSecurityContext(updatedMember, "ROLE_DESIGNER");

        return makeResult(HttpStatus.OK, new HairDesignerProfileDto(hairDesignerProfile));
    }

    @Transactional
    public ResponseEntity<ResultDto> saveImage(Member member, AmazonS3Service amazonS3Service) {
        if (member == null) {
            log.error("[POST] /api/v1/hair_designers/image - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        Member hairDesigner = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);

        if (hairDesigner.getDesignerFlag() == 0) {
            log.error("[POST] /api/v1/hair_designers/image - 400 (?????? ????????? ??????????????? ?????????)");
            return makeResult(HttpStatus.BAD_REQUEST, "??? ????????? ?????? ??????????????? ????????????.");
        }
        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);

        if (hairDesignerProfile == null) {
            log.error("[POST] /api/v1/hair_designers/image - 404 (?????? ????????? ???????????? ??????)");
            return makeResult(HttpStatus.NOT_FOUND, "?????? ????????? ?????? ???????????? ???????????? ????????????.");
        }
        // presigned url ??????
        String preSignedUrl = amazonS3Service.generatePreSignedUrl(cloudFrontUrlHandler.getProfileOfDesignerS3Path(hairDesigner.getId()));

        hairDesignerProfile.setImageUrl(cloudFrontUrlHandler.getProfileOfDesignerImageUrl(hairDesigner.getId()));
        hairDesigner.setImageUrl(cloudFrontUrlHandler.getProfileOfDesignerImageUrl(hairDesigner.getId()));

        // ?????? ?????? X (image url ??????)
        PrincipalDetailsUpdater.setAuthenticationOfSecurityContext(hairDesigner, "ROLE_DESIGNER");

        return makeResult(HttpStatus.OK, new HairDesignerProfileImageResponseDto(preSignedUrl));
    }

    @Transactional
    public ResponseEntity<ResultDto> findAllByName(Member member, String name, Pageable pageable) {
        if (member == null) {
            log.error("[GET] /api/v1/hair_designers/list_by_name - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        List<HairDesignerProfile> hairDesignerProfileList = hairDesignerProfileRepository.findAllByNameAndStatus(name
                , StatusKind.NORMAL.getId(), pageable);

        List<HairDesignerProfileAndHashtagDto> hairDesignerProfileAndHashtagDtoList = hairDesignerProfileList.stream()
                .map(hairDesignerProfile -> new HairDesignerProfileAndHashtagDto(new HairDesignerProfileDto(hairDesignerProfile)
                        , reviewRepository.calculateByHairDesignerProfileIdAndStatus(hairDesignerProfile.getId(), StatusKind.NORMAL.getId())
                        , hairDesignerProfile.getHairDesignerHashtagSet().stream().map(HairDesignerHashtagDto::new).collect(Collectors.toList())))
                .collect(Collectors.toList());

        return makeResult(HttpStatus.OK, hairDesignerProfileAndHashtagDtoList);
    }

    @Transactional
    public ResponseEntity<ResultDto> deleteProfile(Member member) {
        if (member == null) {
            log.error("[DEL] /api/v1/hair_designers - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        Member designer = memberRepository.findByIdAndStatus(member.getId(), StatusKind.NORMAL.getId()).orElse(null);
        if (designer == null || designer.getDesignerFlag() != 1 || !designer.getRole().equals("ROLE_DESIGNER")) {
            log.error("[DEL] /api/v1/hair_designers - 400 (???????????? ????????? ??????????????? ?????????)");
            return makeResult(HttpStatus.BAD_REQUEST, "???????????? ?????? ??????????????? ??????????????? ????????????");
        }

        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (hairDesignerProfile == null) {
            log.error("[DEL] /api/v1/hair_designers - 404 (?????? ???????????? ???????????? ???????????? ???????????? ?????????.)");
            return makeResult(HttpStatus.NOT_FOUND, "?????? ???????????? ?????? ???????????? ???????????? ???????????? ????????????.");
        }

        hairDesignerProfileRepository.delete(hairDesignerProfile);
        designer.setImageUrl(null);

        // ?????? ?????? X (image url ??????)
        PrincipalDetailsUpdater.setAuthenticationOfSecurityContext(designer, "ROLE_DESIGNER");

        return makeResult(HttpStatus.OK, new MemberDto(designer));
    }

    @Transactional
    public ResponseEntity<ResultDto> patchImage(Member member, AmazonS3Service amazonS3Service) {
        if (member == null) {
            log.error("[PATCH] /api/v1/hair_designers/image - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        if (member.getDesignerFlag() != 1 || !member.getRole().equals("ROLE_DESIGNER")) {
            log.error("[PATCH] /api/v1/hair_designers/image - 400 (?????? ????????? ??????????????? ?????????)");
            return makeResult(HttpStatus.BAD_REQUEST, "?????? ????????? ??????????????? ????????????.");
        }

        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (hairDesignerProfile == null) {
            log.error("[PATCH] /api/v1/hair_designers/image - 404 (?????? ??????????????? ???????????? ???????????? ?????????.)");
            return makeResult(HttpStatus.NOT_FOUND, "?????? ??????????????? ???????????? ???????????? ????????????.");
        }

        String preSignedUrl = amazonS3Service.generatePreSignedUrl(cloudFrontUrlHandler.getProfileOfDesignerS3Path(member.getId()));

        return makeResult(HttpStatus.OK, new HairDesignerProfileImageResponseDto(preSignedUrl));
    }

    @Transactional
    public ResponseEntity<ResultDto> findMe(Member member) {
        if (member == null) {
            log.error("[GET] /api/v1/hair_designers - 504 (?????? ??????)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }
        if (member.getDesignerFlag() != 1 && !member.getRole().equals("ROLE_DESIGNER")) {
            log.error("[GET] /api/v1/hair_designers - 400 (?????? ????????? ??????????????? ?????????)");
            return makeResult(HttpStatus.BAD_REQUEST, "?????? ????????? ?????? ??????????????? ????????????.");
        }

        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByHairDesignerAndStatus(
                member, StatusKind.NORMAL.getId()
        ).orElse(null);

        if (hairDesignerProfile == null) {
            log.error("[GET] /api/v1/hair_designers - 404 (?????? ??????????????? ???????????? ???????????? ?????????.)");
            return makeResult(HttpStatus.NOT_FOUND, "?????? ???????????? ???????????? ?????? ???????????????.");
        }

        return makeResult(HttpStatus.OK, new HairDesignerProfileDto(hairDesignerProfile));
    }

    @Transactional
    public ResponseEntity<ResultDto> findManyByRating(Member member, Pageable pageable) {
        if (member == null) {
            log.error(makeErrorLog(504, "/api/v1/hair_designers/list_by_rating", "GET", "?????? ??????"));
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "?????? ??????");
        }

        List<HairDesignerProfile> hairDesignerProfileList = hairDesignerProfileRepository.findAllByStatus(StatusKind.NORMAL.getId(), pageable);

        List<HairDesignerProfileAndHashtagDto> hairDesignerProfileAndHashtagDtoList = hairDesignerProfileList.stream()
                .map(hairDesignerProfile -> new HairDesignerProfileAndHashtagDto(new HairDesignerProfileDto(hairDesignerProfile)
                        , reviewRepository.calculateByHairDesignerProfileIdAndStatus(hairDesignerProfile.getId(), StatusKind.NORMAL.getId())
                        , hairDesignerProfile.getHairDesignerHashtagSet().stream().map(HairDesignerHashtagDto::new).collect(Collectors.toList())))
                .collect(Collectors.toList());

        return makeResult(HttpStatus.OK, hairDesignerProfileAndHashtagDtoList);
    }
}
