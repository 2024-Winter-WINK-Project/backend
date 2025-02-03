package com.WinkProject.meeting.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.WinkProject.meeting.domain.Meeting;
import com.WinkProject.member.domain.Member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeetingResponse {
    private Long id;
    private String name;
    private String description;
    private PlaceResponse place;
    private MemberResponse owner;
    private List<MemberResponse> members;
    private SettlementResponse settlement;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;

    public MeetingResponse() {
        this.members = new ArrayList<>();
    }

    public MeetingResponse(Long id, String name, LocalDateTime createdAt) {
        this();
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    @Getter
    @Setter
    public static class PlaceResponse {
        private String name;
        private String address;
        private Double latitude;
        private Double longitude;
    }

    @Getter
    @Setter
    public static class MemberResponse {
        private Long id;
        private String nickname;
        private String profileImage;
    }

    @Getter
    @Setter
    public static class SettlementResponse {
        private String kakaoPayString;
        private String tossPayString;
        private String accountNumber;
    }

    public static MeetingResponse from(Meeting meeting) {
        MeetingResponse response = new MeetingResponse();
        response.setId(meeting.getId());
        response.setName(meeting.getName());
        response.setDescription(meeting.getDescription());
        response.setStartTime(meeting.getStartTime());
        response.setEndTime(meeting.getEndTime());

        if (meeting.getPlace() != null) {
            PlaceResponse placeResponse = new PlaceResponse();
            placeResponse.setName(meeting.getPlace().getName());
            placeResponse.setAddress(meeting.getPlace().getAddress());
            placeResponse.setLatitude(meeting.getPlace().getLatitude());
            placeResponse.setLongitude(meeting.getPlace().getLongitude());
            response.setPlace(placeResponse);
        }

        Member owner = meeting.getMembers().stream()
                .filter(m -> m.getId().equals(meeting.getOwnerId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("모임장을 찾을 수 없습니다."));

        MemberResponse ownerResponse = new MemberResponse();
        ownerResponse.setId(owner.getId());
        ownerResponse.setNickname(owner.getNickname());
        ownerResponse.setProfileImage(owner.getProfileImage());
        response.setOwner(ownerResponse);

        List<MemberResponse> memberResponses = meeting.getMembers().stream()
                .map(member -> {
                    MemberResponse memberResponse = new MemberResponse();
                    memberResponse.setId(member.getId());
                    memberResponse.setNickname(member.getNickname());
                    memberResponse.setProfileImage(member.getProfileImage());
                    return memberResponse;
                })
                .toList();
        response.setMembers(memberResponses);

        if (meeting.getSettlement() != null) {
            SettlementResponse settlementResponse = new SettlementResponse();
            settlementResponse.setKakaoPayString(meeting.getSettlement().getKakaoPayString());
            settlementResponse.setTossPayString(meeting.getSettlement().getTossPayString());
            settlementResponse.setAccountNumber(meeting.getSettlement().getAccountNumber());
            response.setSettlement(settlementResponse);
        }

        response.setCreatedAt(meeting.getCreatedAt());
        return response;
    }
} 