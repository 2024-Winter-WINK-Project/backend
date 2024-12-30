package com.winkproject.meeting.service;

import com.winkproject.meeting.dto.request.MeetingCreateRequest;
import com.winkproject.meeting.dto.request.MeetingUpdateRequest;
import com.winkproject.meeting.dto.response.MeetingResponse;
import com.winkproject.invitation.dto.response.InvitationResponse;
import com.winkproject.invitation.domain.Invitation;
import com.winkproject.meeting.domain.Meeting;
import com.winkproject.member.domain.Member;
import com.winkproject.member.domain.MemberRole;
import com.winkproject.invitation.repository.InvitationRepository;
import com.winkproject.meeting.repository.MeetingRepository;
import com.winkproject.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final MemberRepository memberRepository;
    private final InvitationRepository invitationRepository;
    
    public List<MeetingResponse> getLatestMeetings(int limit, Long userId) {
        return meetingRepository.findLatestMeetingDTOs(userId, PageRequest.of(0, limit));
    }
    
    public List<MeetingResponse> getMeetingsByUserId(Long userId) {
        return memberRepository.findMeetingsByUserId(userId).stream()
                .map(MeetingResponse::from)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public MeetingResponse createMeeting(MeetingCreateRequest request, Long userId) {
        validateMeetingTime(request.getStartTime(), request.getEndTime());
        
        Meeting meeting = request.toEntity();
        meeting = meetingRepository.save(meeting);
        
        Member member = Member.createOwner(meeting, userId);
        memberRepository.save(member);
        
        return MeetingResponse.from(meeting);
    }
    
    @Transactional
    public MeetingResponse updateMeeting(Long meetingId, MeetingUpdateRequest request, Long userId) {
        Meeting meeting = findMeetingAndValidateOwner(meetingId, userId);
        validateMeetingTime(request.getStartTime(), request.getEndTime());
        
        // Update meeting details
        meeting.setName(request.getName());
        meeting.setDescription(request.getDescription());
        meeting.setStartTime(request.getStartTime());
        meeting.setEndTime(request.getEndTime());
        
        // Update place details
        meeting.getPlace().setName(request.getPlace().getName());
        meeting.getPlace().setAddress(request.getPlace().getAddress());
        meeting.getPlace().setLatitude(request.getPlace().getLatitude());
        meeting.getPlace().setLongitude(request.getPlace().getLongitude());
        
        return MeetingResponse.from(meeting);
    }
    
    @Transactional
    public void deleteMeeting(Long meetingId, Long userId) {
        Meeting meeting = findMeetingAndValidateOwner(meetingId, userId);
        meetingRepository.delete(meeting);
    }
    
    @Transactional
    public InvitationResponse createInvitation(Long meetingId, Long userId) {
        Meeting meeting = findMeetingAndValidateOwner(meetingId, userId);
        
        String inviteCode = generateUniqueInviteCode();
        Invitation invitation = new Invitation();
        invitation.setMeeting(meeting);
        invitation.setInviteCode(inviteCode);
        
        invitation = invitationRepository.save(invitation);
        return InvitationResponse.from(invitation);
    }
    
    public InvitationResponse validateInvitation(Long meetingId, String inviteCode) {
        Invitation invitation = invitationRepository.findByMeetingIdAndInviteCode(meetingId, inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 코드입니다."));
                
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("만료된 초대 코드입니다.");
        }
        
        return InvitationResponse.from(invitation);
    }
    
    private Meeting findMeetingAndValidateOwner(Long meetingId, Long userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));
                
        boolean isOwner = meeting.getMembers().stream()
                .anyMatch(member -> member.getMemberId().equals(userId) && member.getRole() == MemberRole.OWNER);
                
        if (!isOwner) {
            throw new IllegalArgumentException("모임장만 이 작업을 수행할 수 있습니다.");
        }
        
        return meeting;
    }
    
    private String generateUniqueInviteCode() {
        int maxAttempts = 5;
        int attempts = 0;
        
        while (attempts < maxAttempts) {
            String code = UUID.randomUUID().toString().substring(0, 8);
            if (!invitationRepository.existsByInviteCode(code)) {
                return code;
            }
            attempts++;
        }
        
        throw new RuntimeException("초대 코드 생성에 실패했습니다.");
    }
    
    private void validateMeetingTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("시작 시간과 종료 시간은 필수입니다.");
        }
        
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("시작 시간은 현재 시간 이후여야 합니다.");
        }
        
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("종료 시간은 시작 시간 이후여야 합니다.");
        }
    }
    
    @Scheduled(cron = "0 0 * * * *") // 매 시 정각마다 실행
    @Transactional
    public void cleanupExpiredInvitations() {
        invitationRepository.deleteAllExpired(LocalDateTime.now());
    }
} 