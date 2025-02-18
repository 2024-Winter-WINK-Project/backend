package com.WinkProject.meeting.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.UUID;

import com.WinkProject.budget.domain.Budget;
import com.WinkProject.budget.domain.BudgetDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.WinkProject.invitation.dto.response.InvitationResponse;
import com.WinkProject.meeting.domain.Meeting;
import com.WinkProject.meeting.domain.Place;
import com.WinkProject.meeting.domain.Settlement;
import com.WinkProject.meeting.dto.request.MeetingCreateRequest;
import com.WinkProject.meeting.dto.request.MeetingUpdateRequest;
import com.WinkProject.meeting.dto.response.MeetingBriefResponse;
import com.WinkProject.meeting.dto.response.MeetingResponse;
import com.WinkProject.meeting.dto.response.MemberProfileResponse;
import com.WinkProject.meeting.repository.MeetingRepository;
import com.WinkProject.meeting.repository.SettlementRepository;
import com.WinkProject.auth.schema.Auth;
import com.WinkProject.member.domain.Member;
import com.WinkProject.auth.repository.AuthRepository;
import com.WinkProject.invitation.domain.Invitation;
import com.WinkProject.invitation.repository.InvitationRepository;
import com.WinkProject.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final SettlementRepository settlementRepository;
    private final AuthRepository authRepository;
    private final InvitationRepository invitationRepository;
    private final MemberRepository memberRepository;
    
    public List<MeetingBriefResponse> getLatestMeetings(int limit, Long authId) {
        return meetingRepository.findLatestMeetingsByAuthId(authId, limit)
            .stream()
            .map(meeting -> MeetingBriefResponse.from(meeting, authId))
            .collect(Collectors.toList());
    }

    public List<MeetingBriefResponse> getMeetingsByAuthId(Long authId) {
        List<Meeting> meetings = meetingRepository.findMeetingsByAuthId(authId);
        return meetings.stream()
            .map(meeting -> MeetingBriefResponse.from(meeting, authId))
            .collect(Collectors.toList());
    }

    @Transactional
    public MeetingResponse createMeeting(MeetingCreateRequest request, Long authId, String nickname) {
        // 1. Auth 조회
        Auth auth = authRepository.findById(authId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. Meeting 엔티티 생성
        Meeting meeting = request.toEntity();
        meeting.setOwnerId(authId);  // 모임장 설정

        // 3. 모임장을 첫 번째 멤버로 추가
        Member owner = Member.createMember(meeting, auth, nickname);
        meeting.getMembers().add(owner);

        // 4. Settlement 정보가 있는 경우 Settlement 엔티티 생성 및 연결
        if (request.getSettlement() != null) {
            Budget budget = new Budget(meeting);
            budget.setTotalAmount(0L);
            budget.setKakaoRemitLink(request.getSettlement().getKakaoPayString());
            budget.setTossRemitLink(request.getSettlement().getTossPayString());
            budget.setAccountNumber(request.getSettlement().getAccountNumber());
            budget.setDetails(new ArrayList<>());
            meeting.setBudget(budget);
        }

        // 5. Meeting 저장 (cascade로 인해 Settlement도 함께 저장됨)
        Meeting savedMeeting = meetingRepository.save(meeting);

        // 6. 응답 DTO 반환
        return MeetingResponse.from(savedMeeting);
    }

    @Transactional
    public MeetingResponse updateMeeting(Long meetingId, MeetingUpdateRequest request, Long authId) {
        // 1. 모임 조회
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다."));

        // 2. 권한 확인 (모임장만 수정 가능)
        if (!meeting.isOwner(authId)) {
            throw new IllegalArgumentException("모임장만 모임 정보를 수정할 수 있습니다.");
        }

        // 3. 비어있지 않은 필드만 업데이트
        if (request.getName() != null) {
            meeting.setName(request.getName());
        }
        if (request.getDescription() != null) {
            meeting.setDescription(request.getDescription());
        }
        if (request.getStartTime() != null) {
            meeting.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            meeting.setEndTime(request.getEndTime());
        }
        if (request.getPlace() != null) {
            Place place = meeting.getPlace();
            if (place == null) {
                place = new Place();
                meeting.setPlace(place);
            }
            
            MeetingUpdateRequest.PlaceRequest placeRequest = request.getPlace();
            if (placeRequest.getName() != null) {
                place.setName(placeRequest.getName());
            }
            if (placeRequest.getAddress() != null) {
                place.setAddress(placeRequest.getAddress());
            }
            if (placeRequest.getLatitude() != null) {
                place.setLatitude(placeRequest.getLatitude());
            }
            if (placeRequest.getLongitude() != null) {
                place.setLongitude(placeRequest.getLongitude());
            }
        }

        // 4. 변경사항 저장 및 응답 반환
        Meeting updatedMeeting = meetingRepository.save(meeting);
        return MeetingResponse.from(updatedMeeting);
    }

    @Transactional
    public void deleteMeeting(Long meetingId, Long authId) {
        // 1. 모임 조회
        Meeting meeting = meetingRepository.findById(meetingId)
        .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다."));
        
        // 2. 권한 확인 (모임장만 삭제 가능)
        if (!meeting.isOwner(authId)) {
            throw new IllegalArgumentException("모임장만 모임을 삭제할 수 있습니다.");
        }
        
        // 3. 모임 삭제
        meetingRepository.delete(meeting);
    }
    
    @Transactional
    public void leaveMeeting(Long meetingId, Long authId) {
        // 1. 모임 조회
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다."));

        // 2. 멤버 조회 및 유효성 검사
        Member member = meeting.getMembers().stream()
            .filter(m -> m.getAuth().getId().equals(authId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("모임의 멤버가 아닙니다."));

        // 3. 모임장 탈퇴 제한
        if (meeting.isOwner(authId)) {
            throw new IllegalArgumentException("모임장은 탈퇴할 수 없습니다. 먼저 모임장 위임이 필요합니다.");
        }

        // 4. 탈퇴 처리
        member.setWithdrawn(true);
    }
    
    @Transactional
    public void kickMember(Long meetingId, Long targetAuthId, Long authId) {
        // 1. 모임 조회
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다."));

        // 2. 현재 사용자가 모임장인지 확인
        if (!meeting.isOwner(authId)) {
            throw new IllegalArgumentException("모임장만 멤버를 강제 퇴장시킬 수 있습니다.");
        }

        // 3. 강제 퇴장할 멤버가 모임의 멤버인지 확인
        Member targetMember = meeting.getMembers().stream()
            .filter(m -> m.getAuth().getId().equals(targetAuthId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("퇴장시킬 멤버가 모임에 속해있지 않습니다."));

        // 4. 이미 탈퇴한 멤버인지 확인
        if (targetMember.isWithdrawn()) {
            throw new IllegalArgumentException("이미 탈퇴한 멤버입니다.");
        }

        // 5. 모임장을 강제 퇴장시키려는 경우 방지
        if (meeting.isOwner(targetAuthId)) {
            throw new IllegalArgumentException("모임장은 강제 퇴장시킬 수 없습니다.");
        }

        // 6. 강제 퇴장 처리
        targetMember.setWithdrawn(true);
        meetingRepository.save(meeting);
    }
    
    @Transactional
    public InvitationResponse createInvitation(Long meetingId, Long authId) {
        // 1. 모임 조회
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다."));

        // 2. 권한 확인 (모임장인지)
        if (!meeting.isOwner(authId)) {
            throw new IllegalArgumentException("모임장만 초대할 수 있습니다.");
        }

        // 3. 기존 초대 코드 확인
        Optional<Invitation> existingInvitation = invitationRepository.findByMeetingId(meetingId);
        if (existingInvitation.isPresent() && existingInvitation.get().getExpiresAt().isAfter(LocalDateTime.now())) {
            return InvitationResponse.from(existingInvitation.get());
        }

        // 4. 새로운 초대 코드 생성 (6자리 영숫자)
        String inviteCode = null;
        int retryCount = 0;
        while (retryCount < 5) {
            StringBuilder code = new StringBuilder();
            String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            for (int i = 0; i < 6; i++) {
                code.append(characters.charAt((int) (Math.random() * characters.length())));
            }
            String newCode = code.toString();
            
            if (!invitationRepository.existsByInviteCode(newCode)) {
                inviteCode = newCode;
                break;
            }
            retryCount++;
        }
        
        if (inviteCode == null) {
            throw new RuntimeException("초대 코드 생성에 실패했습니다. (5번 시도 후 실패)");
        }

        // 5. 초대 코드 저장
        Invitation invitation = new Invitation();
        invitation.setMeeting(meeting);
        invitation.setInviteCode(inviteCode);
        invitation = invitationRepository.save(invitation);

        // 6. 응답 반환
        return InvitationResponse.from(invitation);
    }
    
    public String getInvitationCode(Long meetingId, Long authId) {
        // 1. 모임 조회
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다."));

        // 2. 권한 확인 (모임장인지)
        if (!meeting.isOwner(authId)) {
            throw new IllegalArgumentException("모임장만 초대 링크를 조회할 수 있습니다.");
        }

        // 3. 초대 코드 조회
        Invitation invitation = invitationRepository.findByMeetingId(meetingId)
            .orElseThrow(() -> new IllegalArgumentException("초대 코드를 먼저 생성해주세요."));

        // 4. 만료 여부 확인
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("만료된 초대 코드입니다. 새로운 초대 코드를 생성해주세요.");
        }

        // 5. 초대 코드 생성 및 반환
        return invitation.getInviteCode();
    }
    
    @Transactional
    public void requestJoinMeeting(String invitationCode, String nickname, Long authId) {
        // 1. 초대 코드로 초대장 조회
        Invitation invitation = invitationRepository.findByInviteCode(invitationCode)
            .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 코드입니다."));

        // 2. 초대장 만료 여부 확인
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("만료된 초대 코드입니다.");
        }

        // 3. 모임 조회
        Meeting meeting = invitation.getMeeting();

        // 4. 이미 모임의 멤버인지 확인
        boolean isAlreadyMember = meeting.getMembers().stream()
            .anyMatch(m -> m.getAuth().getId().equals(authId) && !m.isWithdrawn());
        if (isAlreadyMember) {
            throw new IllegalArgumentException("이미 모임의 멤버입니다.");
        }

        // 5. 닉네임 중복 확인
        if (memberRepository.existsByMeetingIdAndNicknameAndIsWithdrawnFalse(meeting.getId(), nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 6. Auth 조회
        Auth auth = authRepository.findById(authId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 7. 멤버 생성 및 추가
        Member newMember = Member.createMember(meeting, auth, nickname);
        meeting.getMembers().add(newMember);
        meetingRepository.save(meeting);
    }
    
    public List<MemberProfileResponse> getMeetingMembers(Long meetingId) {
        // 1. 모임 조회
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다."));

        // 2. 멤버 목록을 MemberProfileResponse로 변환
        return meeting.getMembers().stream()
            .filter(member -> !member.isWithdrawn())  // 탈퇴하지 않은 멤버만 필터링
            .map(member -> MemberProfileResponse.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImage())
                .build())
            .collect(Collectors.toList());
    }

    @Transactional
    public void delegateOwner(Long meetingId, Long currentAuthId, Long newAuthId) {
        // 1. 모임 조회
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다."));

        // 2. 현재 사용자가 모임장인지 확인
        if (!meeting.isOwner(currentAuthId)) {
            throw new IllegalArgumentException("모임장만 권한을 위임할 수 있습니다.");
        }

        // 3. 새로운 모임장이 될 멤버가 모임의 멤버인지 확인
        Member newOwner = meeting.getMembers().stream()
            .filter(m -> m.getAuth().getId().equals(newAuthId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("위임할 멤버가 모임에 속해있지 않습니다."));

        // 4. 새로운 모임장이 탈퇴한 멤버인지 확인
        if (newOwner.isWithdrawn()) {
            throw new IllegalArgumentException("탈퇴한 멤버에게 모임장을 위임할 수 없습니다.");
        }

        // 5. 모임장 권한 위임
        meeting.setOwnerId(newAuthId);
        meetingRepository.save(meeting);
    }

    public MeetingResponse getMeetingDetail(Long meetingId) {
        // 1. 모임 조회
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다."));

        // 2. 상세 정보를 포함한 응답 반환
        return MeetingResponse.from(meeting);
    }

}   