package com.WinkProject.meeting.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.WinkProject.invitation.repository.InvitationRepository;
import com.WinkProject.meeting.domain.Meeting;
import com.WinkProject.meeting.domain.Settlement;
import com.WinkProject.meeting.dto.request.MeetingCreateRequest;
import com.WinkProject.meeting.dto.request.MeetingUpdateRequest;
import com.WinkProject.meeting.dto.response.InvitationResponse;
import com.WinkProject.meeting.dto.response.MeetingResponse;
import com.WinkProject.meeting.dto.response.MemberProfileResponse;
import com.WinkProject.meeting.repository.MeetingRepository;
import com.WinkProject.member.repository.MemberRepository;
import com.WinkProject.member.domain.Member;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final MemberRepository memberRepository;
    private final InvitationRepository invitationRepository;

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
        // 1. Meeting 엔티티 생성
        Meeting meeting = request.toEntity();
        meeting.setOwnerId(userId);  // 모임장 설정

        // 2. Settlement 정보가 있는 경우 Settlement 엔티티 생성 및 연결
        if (request.getSettlement() != null) {
            Settlement settlement = new Settlement(request.getSettlement());
            meeting.setSettlement(settlement);
        }

        // 3. 모임장을 첫 번째 멤버로 추가
        Member owner = Member.createMember(meeting, userId, "모임장");
        meeting.getMembers().add(owner);

        // 4. Meeting 저장 (Settlement와 Member는 CascadeType.ALL로 자동 저장)
        Meeting savedMeeting = meetingRepository.save(meeting);

        // 5. 응답 DTO 반환
        return MeetingResponse.from(savedMeeting);
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
        // 1. 현재 사용자가 모임장인지 확인
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다."));
        
        if (!meeting.isOwner(currentOwnerId)) {
            throw new IllegalArgumentException("모임장만 권한을 위임할 수 있습니다.");
        }

        // 2. 새로운 모임장이 모임의 멤버인지 확인 및 권한 위임
        meeting.changeOwner(newOwnerId);
        meetingRepository.save(meeting);
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