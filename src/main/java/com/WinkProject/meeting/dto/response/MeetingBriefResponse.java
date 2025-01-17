package com.WinkProject.meeting.dto.response;

import java.time.LocalDateTime;

import com.WinkProject.meeting.domain.Meeting;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MeetingBriefResponse {
    private Long id;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isOwner;

    public static MeetingBriefResponse from(Meeting meeting, Long userId) {
        MeetingBriefResponse response = new MeetingBriefResponse();
        response.setId(meeting.getId());
        response.setName(meeting.getName());
        response.setStartTime(meeting.getStartTime());
        response.setEndTime(meeting.getEndTime());
        response.setOwner(meeting.getOwnerId().equals(userId));
        return response;
    }
}
