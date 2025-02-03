package com.WinkProject.meeting.dto.request;

import java.time.LocalDateTime;

import com.WinkProject.meeting.domain.Meeting;
import com.WinkProject.meeting.domain.Place;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeetingCreateRequest {
    private String name;
    private PlaceRequest place;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private SettlementRequest settlement;

    @Getter
    @Setter
    public static class PlaceRequest {
        private String name;
        private String address;
        private Double latitude;
        private Double longitude;
    }

    @Getter
    @Setter
    public static class SettlementRequest {
        private String kakaoPayString;
        private String tossPayString;
        private String accountNumber;
    }

    public Meeting toEntity() {
        Meeting meeting = new Meeting();
        meeting.setName(this.name);
        meeting.setStartTime(this.startTime);
        meeting.setEndTime(this.endTime);

        Place place = new Place();
        place.setName(this.place.name);
        place.setAddress(this.place.address);
        place.setLatitude(this.place.latitude);
        place.setLongitude(this.place.longitude);
        meeting.setPlace(place);

        return meeting;
    }
} 