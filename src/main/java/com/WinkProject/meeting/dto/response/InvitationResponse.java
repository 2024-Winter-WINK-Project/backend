package com.WinkProject.meeting.dto.response;

import com.WinkProject.invitation.domain.Invitation;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InvitationResponse {
    private Long id;
    private String inviteCode;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public static InvitationResponse from(Invitation invitation) {
        InvitationResponse response = new InvitationResponse();
        response.setId(invitation.getId());
        response.setInviteCode(invitation.getInviteCode());
        response.setExpiresAt(invitation.getExpiresAt());
        response.setCreatedAt(invitation.getCreatedAt());
        return response;
    }
} 