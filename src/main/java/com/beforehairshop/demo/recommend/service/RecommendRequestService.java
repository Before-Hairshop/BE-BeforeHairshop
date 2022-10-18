package com.beforehairshop.demo.recommend.service;

import com.beforehairshop.demo.constant.member.StatusKind;
import com.beforehairshop.demo.hairdesigner.domain.HairDesignerProfile;
import com.beforehairshop.demo.hairdesigner.repository.HairDesignerProfileRepository;
import com.beforehairshop.demo.member.domain.Member;
import com.beforehairshop.demo.member.domain.MemberProfile;
import com.beforehairshop.demo.member.dto.MemberProfileDto;
import com.beforehairshop.demo.member.repository.MemberProfileRepository;
import com.beforehairshop.demo.member.repository.MemberRepository;
import com.beforehairshop.demo.recommend.domain.RecommendRequest;
import com.beforehairshop.demo.recommend.dto.RecommendRequestDto;
import com.beforehairshop.demo.recommend.dto.post.RecommendRequestSaveRequestDto;
import com.beforehairshop.demo.recommend.repository.RecommendRequestRepository;
import com.beforehairshop.demo.response.ResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.beforehairshop.demo.response.ResultDto.makeResult;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendRequestService {

    private final RecommendRequestRepository recommendRequestRepository;
    private final MemberRepository memberRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final HairDesignerProfileRepository hairDesignerProfileRepository;


    @Transactional
    public ResponseEntity<ResultDto> save(Member member, RecommendRequestSaveRequestDto saveRequestDto) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");

        if (!saveDtoIsValid(saveRequestDto))
            return makeResult(HttpStatus.NO_CONTENT, "스타일 추천 요청서를 저장하기 위한 정보가 부족하다.");

        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(
                member, StatusKind.NORMAL.getId()
        ).orElse(null);

        if (memberProfile == null)
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "유저 프로필이 등록되어 있지 않아 스타일 추천 요청서를 작성할 수 없다.");

        HairDesignerProfile hairDesignerProfile = hairDesignerProfileRepository.findByIdAndStatus(
                saveRequestDto.getHairDesignerProfileId(), StatusKind.NORMAL.getId()
        ).orElse(null);

        if (hairDesignerProfile == null)
            return makeResult(HttpStatus.BAD_REQUEST, "잘못된 디자이너 프로필 ID 가 입력되었다.");

        RecommendRequest checkRequest = recommendRequestRepository.findByToRecommendRequestProfileAndFromRecommendRequestProfileAndStatus(
                hairDesignerProfile, memberProfile, StatusKind.NORMAL.getId()
        ).orElse(null);

        // 이게 말이 되나?? 이렇게 되면, 프로필 수정하고 추천 요청서를 다시 작성할 수가 없다... 흠,, 말이 안 됨
        if (checkRequest != null) {
            return makeResult(HttpStatus.CONFLICT, "이미 추천 요청서를 보냈다.");
        }

        RecommendRequest recommendRequest = new RecommendRequest(hairDesignerProfile, memberProfile, StatusKind.NORMAL.getId());
        recommendRequest = recommendRequestRepository.save(recommendRequest);

        hairDesignerProfile.addToRecommendRequest(recommendRequest);
        memberProfile.addFromRecommendRequest(recommendRequest);

        return makeResult(HttpStatus.OK, new RecommendRequestDto(recommendRequest));
    }

    private boolean saveDtoIsValid(RecommendRequestSaveRequestDto saveRequestDto) {
        return saveRequestDto.getHairDesignerProfileId() != null;
    }

    @Transactional
    public ResponseEntity<ResultDto> delete(Member member, BigInteger recommendRequestId) {
        if (member == null)
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");

        RecommendRequest recommendRequest = recommendRequestRepository.findByIdAndStatus(recommendRequestId, StatusKind.NORMAL.getId()).orElse(null);
        if (recommendRequest == null)
            return makeResult(HttpStatus.NOT_FOUND, "잘못된 추천 요청서 ID 입니다.");

        MemberProfile memberProfile = memberProfileRepository.findByMemberAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (memberProfile == null)
            return makeResult(HttpStatus.NOT_FOUND, "유저 프로필이 존재하지 않습니다.");

        if (!recommendRequest.getFromRecommendRequestProfile().getId().equals(memberProfile.getId()))
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "해당 추천 요청서를 삭제할 권한이 없는 유저입니다.");

        recommendRequestRepository.delete(recommendRequest);

        return makeResult(HttpStatus.OK, "삭제 완료");
    }

    @Transactional
    public ResponseEntity<ResultDto> findManyByDesigner(Member member) {
        if (member == null) {
            log.error("[GET] /api/v1/recommend/request/list_by_designer - 504 (세션 만료)");
            return makeResult(HttpStatus.GATEWAY_TIMEOUT, "세션 만료");
        }

        if (member.getDesignerFlag() != 1 || !member.getRole().equals("ROLE_DESIGNER")) {
            log.error("[GET] /api/v1/recommend/request/list_by_designer - 503 (헤어 디자이너가 아님) : member_id = " + member.getId());
            return makeResult(HttpStatus.SERVICE_UNAVAILABLE, "해당 유저는 헤어 디자이너가 아닙니다");
        }
        HairDesignerProfile hairDesignerProfile
                = hairDesignerProfileRepository.findByHairDesignerAndStatus(member, StatusKind.NORMAL.getId()).orElse(null);
        if (hairDesignerProfile == null) {
            log.error("[GET] /api/v1/recommend/request/list_by_designer - 404 (헤어 디자이너 프로필이 등록되어 있지 않습니다) : member_id = " + member.getId());
            return makeResult(HttpStatus.NOT_FOUND, "디자이너 프로필이 등록되어 있지 않습니다.");
        }

        if (hairDesignerProfile.getToRecommendRequestSet() == null) {
            return makeResult(HttpStatus.OK, null);
        }

        List<RecommendRequest> recommendRequestList = recommendRequestRepository.findByToRecommendRequestProfileAndStatusOrderByCreateDateDesc(
                hairDesignerProfile, StatusKind.NORMAL.getId()
        );

        List<MemberProfileDto> memberProfileDtoList = recommendRequestList.stream()
                .map(RecommendRequest::getFromRecommendRequestProfile).collect(Collectors.toList())
                .stream()
                .map(MemberProfileDto::new).collect(Collectors.toList());

//        List<MemberProfileDto> memberProfileDtoList = hairDesignerProfile.getToRecommendRequestSet().stream()
//                .map(RecommendRequest::getFromRecommendRequestProfile)
//                .collect(Collectors.toList())
//                .stream().map(MemberProfileDto::new).collect(Collectors.toList());

        // 매칭 플래그 1인지, 겹쳐지지는 않는지, 정렬되었는지 확인할 것!

        return makeResult(HttpStatus.OK, memberProfileDtoList);

    }
}
