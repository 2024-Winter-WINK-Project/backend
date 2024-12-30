package com.winkproject.meeting.dto.request;

import com.winkproject.meeting.domain.Meeting;
import com.winkproject.meeting.domain.Place;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class MeetingCreateRequest {
    private String name;
    private String description;
    private PlaceRequest place;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    @Getter @Setter
    public static class PlaceRequest {
        private String name;
        private String address;
        private Double latitude;
        private Double longitude;
    }
    
    public Meeting toEntity() {
        Meeting meeting = new Meeting();
        meeting.setName(this.name);
        meeting.setDescription(this.description);
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