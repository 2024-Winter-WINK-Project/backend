package com.winkproject.meeting.dto.response;

import com.winkproject.meeting.domain.Meeting;
import com.winkproject.member.domain.Member;
import com.winkproject.member.domain.MemberRole;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class MeetingResponse {
    private Long id;
    private String name;
    private String description;
    private PlaceResponse place;
    private MemberResponse owner;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    
    public MeetingResponse() {
    }
    
    public MeetingResponse(Long id, String name, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }
    
    @Getter @Setter
    public static class PlaceResponse {
        private String name;
        private String address;
        private Double latitude;
        private Double longitude;
    }
    
    @Getter @Setter
    public static class MemberResponse {
        private Long id;
        private String nickname;
        private String profileImage;
    }
    
    public static MeetingResponse from(Meeting meeting) {
        MeetingResponse response = new MeetingResponse();
        response.setId(meeting.getId());
        response.setName(meeting.getName());
        response.setDescription(meeting.getDescription());
        response.setStartTime(meeting.getStartTime());
        response.setEndTime(meeting.getEndTime());
        
        PlaceResponse placeResponse = new PlaceResponse();
        placeResponse.setName(meeting.getPlace().getName());
        placeResponse.setAddress(meeting.getPlace().getAddress());
        placeResponse.setLatitude(meeting.getPlace().getLatitude());
        placeResponse.setLongitude(meeting.getPlace().getLongitude());
        response.setPlace(placeResponse);
        
        Member owner = meeting.getMembers().stream()
                .filter(m -> m.getRole() == MemberRole.OWNER)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("모임장을 찾을 수 없습니다."));
                
        MemberResponse ownerResponse = new MemberResponse();
        ownerResponse.setId(owner.getMemberId());
        ownerResponse.setNickname(owner.getNickname());
        ownerResponse.setProfileImage(owner.getDefaultProfileImage());
        response.setOwner(ownerResponse);
        
        response.setCreatedAt(meeting.getCreatedAt());
        return response;
    }
} 