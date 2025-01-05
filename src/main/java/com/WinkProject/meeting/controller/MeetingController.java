package com.WinkProject.meeting.controller;

import com.WinkProject.invitation.dto.response.InvitationResponse;
import com.WinkProject.meeting.dto.request.MeetingCreateRequest;
import com.WinkProject.meeting.dto.request.MeetingUpdateRequest;
import com.WinkProject.meeting.dto.response.MeetingResponse;
import com.WinkProject.meeting.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {
    private final MeetingService meetingService;

    @GetMapping("/latest")
    public ResponseEntity<List<MeetingResponse>> getLatestMeetings(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam Long userId) {
        return ResponseEntity.ok(meetingService.getLatestMeetings(limit, userId));
    }

    @GetMapping
    public ResponseEntity<List<MeetingResponse>> getMeetings(@RequestParam Long userId) {
        return ResponseEntity.ok(meetingService.getMeetingsByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<MeetingResponse> createMeeting(
            @RequestBody MeetingCreateRequest request,
            @RequestParam Long userId) {
        return ResponseEntity.ok(meetingService.createMeeting(request, userId));
    }

    @PutMapping("/{meetingId}")
    public ResponseEntity<MeetingResponse> updateMeeting(
            @PathVariable Long meetingId,
            @RequestBody MeetingUpdateRequest request,
            @RequestParam Long userId) {
        return ResponseEntity.ok(meetingService.updateMeeting(meetingId, request, userId));
    }

    @DeleteMapping("/{meetingId}")
    public ResponseEntity<Void> deleteMeeting(
            @PathVariable Long meetingId,
            @RequestParam Long userId) {
        meetingService.deleteMeeting(meetingId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{meetingId}/invitations")
    public ResponseEntity<InvitationResponse> createInvitation(
            @PathVariable Long meetingId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(meetingService.createInvitation(meetingId, userId));
    }

    @GetMapping("/{meetingId}/invitations/{invitationCode}")
    public ResponseEntity<InvitationResponse> validateInvitation(
            @PathVariable Long meetingId,
            @PathVariable String invitationCode) {
        return ResponseEntity.ok(meetingService.validateInvitation(meetingId, invitationCode));
    }
} 