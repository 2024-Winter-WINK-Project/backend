package com.WinkProject.invitation.dto.response;

import com.WinkProject.invitation.domain.Invitation;
import com.WinkProject.meeting.dto.response.MeetingResponse;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InvitationResponse {
    private String inviteCode;
    private LocalDateTime expiresAt;
    private MeetingResponse meeting;

    public static InvitationResponse from(Invitation invitation) {
        InvitationResponse response = new InvitationResponse();
        response.setInviteCode(invitation.getInviteCode());
        response.setExpiresAt(invitation.getExpiresAt());
        response.setMeeting(MeetingResponse.from(invitation.getMeeting()));
        return response;
    }
} 