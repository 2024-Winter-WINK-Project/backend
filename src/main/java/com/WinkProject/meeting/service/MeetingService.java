package com.WinkProject.meeting.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.WinkProject.meeting.domain.Meeting;
import com.WinkProject.meeting.domain.Settlement;
import com.WinkProject.meeting.dto.request.MeetingCreateRequest;
import com.WinkProject.meeting.dto.request.MeetingUpdateRequest;
import com.WinkProject.meeting.dto.response.InvitationResponse;
import com.WinkProject.meeting.dto.response.MeetingResponse;
import com.WinkProject.meeting.dto.response.MemberProfileResponse;
import com.WinkProject.meeting.repository.MeetingRepository;
import com.WinkProject.meeting.repository.SettlementRepository;
import com.WinkProject.member.domain.Auth;
import com.WinkProject.member.domain.Member;
import com.WinkProject.member.repository.AuthRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final SettlementRepository settlementRepository;
    private final AuthRepository authRepository;
    
    public List<MeetingResponse> getLatestMeetings(int limit, Long userId) {
        // TODO: Implement logic
        return new ArrayList<>();
    }

    public List<MeetingResponse> getMeetingsByUserId(Long userId) {
        // TODO: Implement logic
        return new ArrayList<>();
    }

    @Transactional
    public MeetingResponse createMeeting(MeetingCreateRequest request, Long userId) {
        // 1. Auth 조회
        Auth auth = authRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. Meeting 엔티티 생성
        Meeting meeting = request.toEntity();
        meeting.setOwnerId(userId);  // 모임장 설정

        // 3. 모임장을 첫 번째 멤버로 추가
        Member owner = Member.createMember(meeting, auth, "모임장");
        meeting.getMembers().add(owner);

        // 4. Meeting 저장
        Meeting savedMeeting = meetingRepository.save(meeting);

        // 5. Settlement 정보가 있는 경우 Settlement 엔티티 생성 및 연결
        if (request.getSettlement() != null) {
            createSettlement(request, savedMeeting);
        }

        // 6. 응답 DTO 반환
        return MeetingResponse.from(savedMeeting);
    }

    @Transactional
    private void createSettlement(MeetingCreateRequest request, Meeting savedMeeting) {
        Settlement settlement = Settlement.from(request.getSettlement(), savedMeeting);
        savedMeeting.setSettlement(settlement);
        settlementRepository.save(settlement);
    }

    public MeetingResponse updateMeeting(Long meetingId, MeetingUpdateRequest request, Long userId) {
        // TODO: Implement logic
        return new MeetingResponse();
    }

    public void deleteMeeting(Long meetingId, Long userId) {
        // TODO: Implement logic
    }

    public InvitationResponse createInvitation(Long meetingId, Long userId) {
        // TODO: Implement logic
        return new InvitationResponse();
    }

    public boolean validateInvitation(Long meetingId, String invitationCode) {
        // TODO: Implement logic
        return false;
    }

    public void kickMember(Long meetingId, Long targetUserId, Long userId) {
        // TODO: 모임 멤버 강제 퇴장 로직 구현
    }

    public String getInvitationLink(Long meetingId, Long userId) {
        // TODO: 모임 초대 링크 조회 로직 구현
        return "";
    }

    @Transactional
    public void requestJoinMeeting(String invitationCode, String nickname) {
        // TODO: 모임 가입 신청 로직 구현
    }

    public void leaveMeeting(Long meetingId, Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'leaveMeeting'");
    }

    public List<MemberProfileResponse> getMeetingMembers(Long meetingId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMeetingMembers'");
    }

    public void delegateOwner(Long meetingId, Long currentOwnerId, Long newOwnerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delegateOwner'");
    }

    public Object getMeetingDetail(Long meetingId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMeetingDetail'");
    }

    public void delegateLeader(Long meetingId, Long userId, Long newLeaderId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delegateLeader'");
    }
} 