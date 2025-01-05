package com.WinkProject.meeting.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MeetingUpdateRequest {
    private String name;
    private String description;
    private PlaceRequest place;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Getter
    @Setter
    public static class PlaceRequest {
        private String name;
        private String address;
        private Double latitude;
        private Double longitude;
    }
} 