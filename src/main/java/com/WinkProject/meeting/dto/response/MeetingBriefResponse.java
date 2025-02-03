package com.WinkProject.meeting.dto.response;

import java.time.LocalDateTime;

import com.WinkProject.meeting.domain.Meeting;
import com.WinkProject.meeting.domain.Place;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingBriefResponse {
    private Long id;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private PlaceResponse place;
    private boolean isOwner;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceResponse {
        private String name;
        private String address;
        private Double latitude;
        private Double longitude;

        public static PlaceResponse from(Place place) {
            if (place == null) {
                return null;
            }
            return PlaceResponse.builder()
                    .name(place.getName())
                    .address(place.getAddress())
                    .latitude(place.getLatitude())
                    .longitude(place.getLongitude())
                    .build();
        }
    }

    public static MeetingBriefResponse from(Meeting meeting, Long authId) {
        return MeetingBriefResponse.builder()
                .id(meeting.getId())
                .name(meeting.getName())
                .startTime(meeting.getStartTime())
                .endTime(meeting.getEndTime())
                .place(PlaceResponse.from(meeting.getPlace()))
                .isOwner(meeting.isOwner(authId))
                .build();
    }
}
