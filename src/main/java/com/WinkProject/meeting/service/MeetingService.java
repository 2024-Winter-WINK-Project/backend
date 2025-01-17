package com.WinkProject.meeting.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.WinkProject.meeting.domain.Meeting;
import com.WinkProject.meeting.domain.Place;
import com.WinkProject.meeting.domain.Settlement;
import com.WinkProject.meeting.dto.request.MeetingCreateRequest;
import com.WinkProject.meeting.dto.request.MeetingUpdateRequest;
import com.WinkProject.meeting.dto.response.InvitationResponse;
import com.WinkProject.meeting.dto.response.MeetingBriefResponse;
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
    
    public List<MeetingBriefResponse> getLatestMeetings(int limit, Long userId) {
        return meetingRepository.findLatestMeetings(userId, limit)
            .stream()
            .map(meeting -> MeetingBriefResponse.from(meeting, userId))
            .collect(Collectors.toList());
    }

    public List<MeetingBriefResponse> getMeetingsByUserId(Long userId) {
        List<Meeting> meetings = meetingRepository.findMeetingsByUserId(userId);
        return meetings.stream()
            .map(meeting -> MeetingBriefResponse.from(meeting, userId))
            .collect(Collectors.toList());
    }

    @Transactional
    public MeetingResponse createMeeting(MeetingCreateRequest request, Long userId, String nickname) {
        // 1. Auth 조회
        Auth auth = authRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. Meeting 엔티티 생성
        Meeting meeting = request.toEntity();
        meeting.setOwnerId(userId);  // 모임장 설정

        // 3. 모임장을 첫 번째 멤버로 추가
        Member owner = Member.createMember(meeting, auth, nickname);
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

    @Transactional
    public MeetingResponse updateMeeting(Long meetingId, MeetingUpdateRequest request, Long userId) {
        // 1. 모임 조회
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다."));

        // 2. 권한 확인 (모임장만 수정 가능)
        if (!meeting.isOwner(userId)) {
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

    public void delegateOwner(Long meetingId, Long currentOwnerId, Long newOwnerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delegateOwner'");
    }

    public MeetingResponse getMeetingDetail(Long meetingId) {
        // 1. 모임 조회
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다."));

        // 2. 상세 정보를 포함한 응답 반환
        return MeetingResponse.from(meeting);
    }

    public void delegateLeader(Long meetingId, Long userId, Long newLeaderId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delegateLeader'");
    }
} 