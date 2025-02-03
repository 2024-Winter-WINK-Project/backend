package com.WinkProject.meeting.controller;

import com.WinkProject.meeting.dto.response.MeetingBriefResponse;
import com.WinkProject.meeting.dto.response.MemberProfileResponse;
import com.WinkProject.invitation.dto.response.InvitationResponse;
import com.WinkProject.meeting.dto.request.MeetingCreateRequest;
import com.WinkProject.meeting.dto.request.MeetingUpdateRequest;
import com.WinkProject.meeting.dto.response.MeetingResponse;
import com.WinkProject.meeting.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {
    private final MeetingService meetingService;

    @Operation(
        summary = "내가 만든/가입한 모임 조회",
        description = "최근 N=5개 모임",
        tags = {"1. 모임 조회"}
    )
    @Parameter(
        name = "limit",
        description = "조회할 모임 개수 (기본값: 5)",
        schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "5")
    )
    @Parameter(
        name = "authId",
        description = "Auth ID (인증 기능 연동 후 제거 예정)",
        required = true
    )
    @GetMapping("/latest")
    public ResponseEntity<List<MeetingBriefResponse>> getLatestMeetings(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam Long authId) {
        return ResponseEntity.ok(meetingService.getLatestMeetings(limit, authId));
    }

    @Operation(
        summary = "내가 만든/가입한 모임 전체 조회",
        description = "사용자가 만들었거나 가입한 모든 모임을 조회합니다",
        tags = {"1. 모임 조회"}
    )
    @Parameter(
        name = "authId", 
        description = "Auth ID (인증 기능 연동 후 제거 예정)",
        required = true
    )
    @GetMapping
    public ResponseEntity<List<MeetingBriefResponse>> getMeetings(@RequestParam Long authId) {
        return ResponseEntity.ok(meetingService.getMeetingsByAuthId(authId));
    }

    @Operation(
        summary = "모임 멤버 목록 조회",
        description = "모임에 소속된 멤버들의 정보를 조회합니다",
        tags = {"1. 모임 조회"}
    )
    @Parameter(
        name = "meetingId",
        description = "조회할 모임 ID",
        required = true
    )
    @GetMapping("/{meetingId}/members")
    public ResponseEntity<List<MemberProfileResponse>> getMeetingMembers(
            @PathVariable Long meetingId) {
        return ResponseEntity.ok(meetingService.getMeetingMembers(meetingId));
    }

    @Operation(
        summary = "모임 상세 정보 조회",
        description = "특정 모임의 상세 정보를 조회합니다",
        tags = {"1. 모임 조회"}
    )
    @Parameter(
        name = "meetingId",
        description = "조회할 모임 ID",
        required = true
    )
    @GetMapping("/{meetingId}")
    public ResponseEntity<MeetingResponse> getMeetingDetail(
            @PathVariable Long meetingId) {
        return ResponseEntity.ok(meetingService.getMeetingDetail(meetingId));
    }

    @Operation(
        summary = "모임 생성",
        description = "새로운 모임을 생성합니다",
        tags = {"2. 모임 생성/수정"}
    )
    @Parameter(
        name = "authId",
        description = "Auth ID (인증 기능 연동 후 제거 예정)",
        required = true
    )
    @Parameter(
        name = "nickname",
        description = "모임에서 사용할 닉네임",
        required = true
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "모임 생성 정보",
        required = true
    )
    @PostMapping
    public ResponseEntity<MeetingResponse> createMeeting(
            @RequestBody MeetingCreateRequest request,
            @RequestParam Long authId,
            @RequestParam String nickname) {
        return ResponseEntity.ok(meetingService.createMeeting(request, authId, nickname));
    }

    @Operation(
        summary = "모임 수정",
        description = "모임의 시간과 장소를 수정합니다",
        tags = {"2. 모임 생성/수정"}
    )
    @Parameter(
        name = "meetingId",
        description = "수정할 모임 ID",
        required = true
    )
    @Parameter(
        name = "authId",
        description = "Auth ID (인증 기능 연동 후 제거 예정)",
        required = true
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "모임 수정 정보",
        required = true
    )
    @PutMapping("/{meetingId}")
    public ResponseEntity<MeetingResponse> updateMeeting(
            @PathVariable Long meetingId,
            @RequestBody MeetingUpdateRequest request,
            @RequestParam Long authId) {
        return ResponseEntity.ok(meetingService.updateMeeting(meetingId, request, authId));
    }

    @Operation(
        summary = "모임 삭제",
        description = "모임을 삭제합니다",
        tags = {"3. 모임 삭제 및 위임"}
    )
    @Parameter(
        name = "meetingId",
        description = "삭제할 모임 ID",
        required = true
    )
    @Parameter(
        name = "authId", 
        description = "Auth ID (인증 기능 연동 후 제거 예정)",
        required = true
    )
    @DeleteMapping("/{meetingId}")
    public ResponseEntity<Void> deleteMeeting(
            @PathVariable Long meetingId,
            @RequestParam Long authId) {
        meetingService.deleteMeeting(meetingId, authId);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "모임장 위임하기",
        description = "다른 멤버에게 모임장 권한을 위임합니다",
        tags = {"3. 모임 삭제 및 위임"}
    )
    @Parameter(
        name = "meetingId",
        description = "모임 ID",
        required = true
    )
    @Parameter(
        name = "authId",
        description = "현재 모임장 Auth ID (인증 기능 연동 후 제거 예정)",
        required = true
    )
    @Parameter(
        name = "newLeaderAuthId",
        description = "새로운 모임장이 될 멤버의 Auth ID",
        required = true
    )
    @PostMapping("/{meetingId}/delegate")
    public ResponseEntity<Void> delegateOwner(
            @PathVariable Long meetingId,
            @RequestParam Long authId,
            @RequestParam Long newLeaderAuthId) {
        meetingService.delegateOwner(meetingId, authId, newLeaderAuthId);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "모임 나가기",
        description = "모임에서 탈퇴합니다",
        tags = {"4. 모임 멤버 관리"}
    )
    @Parameter(
        name = "meetingId",
        description = "나갈 모임 ID",
        required = true
    )
    @Parameter(
        name = "authId",
        description = "Auth ID (인증 기능 연동 후 제거 예정)", 
        required = true
    )
    @DeleteMapping("/{meetingId}/members")
    public ResponseEntity<Void> leaveMeeting(
            @PathVariable Long meetingId,
            @RequestParam Long authId) {
        meetingService.leaveMeeting(meetingId, authId);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "모임 멤버 강제 퇴장",
        description = "모임에서 특정 멤버를 강제로 퇴장시킵니다",
        tags = {"4. 모임 멤버 관리"}
    )
    @Parameter(
        name = "meetingId", 
        description = "모임 ID",
        required = true
    )
    @Parameter(
        name = "targetAuthId",
        description = "퇴장시킬 사용자의 Auth ID",
        required = true
    )
    @Parameter(
        name = "authId",
        description = "요청하는 사용자의 Auth ID (인증 기능 연동 후 제거 예정)",
        required = true
    )
    @DeleteMapping("/{meetingId}/members/{targetAuthId}")
    public ResponseEntity<Void> kickMember(
            @PathVariable Long meetingId,
            @PathVariable Long targetAuthId,
            @RequestParam Long authId) {
        meetingService.kickMember(meetingId, targetAuthId, authId);
        return ResponseEntity.ok().build();
    }

    
    @Operation(
        summary = "모임 초대 코드 생성",
        description = "모임에 초대 코드를 생성합니다",
        tags = {"5. 모임 초대"}
    )
    @Parameter(
        name = "meetingId",
        description = "초대 코드를 생성할 모임 ID",
        required = true
    )
    @Parameter(
        name = "authId",
        description = "Auth ID (인증 기능 연동 후 제거 예정)", 
        required = true
    )
    @PostMapping("/{meetingId}/invitations")
    public ResponseEntity<InvitationResponse> createInvitation(
            @PathVariable Long meetingId,
            @RequestParam Long authId) {
        return ResponseEntity.ok(meetingService.createInvitation(meetingId, authId));
    }

    @Operation(
        summary = "모임 초대 링크 조회 (모임 초대 받기)",
        description = "모임의 초대 링크를 조회합니다",
        tags = {"5. 모임 초대"}
    )
    @Parameter(
        name = "meetingId",
        description = "초대 링크를 조회할 모임 ID",
        required = true
    )
    @Parameter(
        name = "authId",
        description = "Auth ID (인증 기능 연동 후 제거 예정)",
        required = true
    )
    @GetMapping("/{meetingId}/invitations")
    public ResponseEntity<String> getInvitationLink(
            @PathVariable Long meetingId,
            @RequestParam Long authId,
            HttpServletRequest request) {

        String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), "");
        String invitationCode = meetingService.getInvitationCode(meetingId, authId);
        String invitationLink = baseUrl + "/meetings/invitations/" + invitationCode + "/request";

        return ResponseEntity.ok(invitationLink);
    }

    @Operation(
        summary = "모임 초대 수락하기",
        description = "초대 코드를 통해 모임에 가입 신청합니다",
        tags = {"5. 모임 초대"}
    )
    @Parameter(
        name = "invitationCode",
        description = "모임 초대 코드",
        required = true
    )
    @Parameter(
        name = "nickname",
        description = "가입 신청자 닉네임",
        required = true
    )
    @Parameter(
        name = "authId",
        description = "Auth ID (인증 기능 연동 후 제거 예정)",
        required = true
    )
    @PostMapping("/invitations/{invitationCode}/request")
    public ResponseEntity<Void> requestJoinMeeting(
            @PathVariable String invitationCode,
            @RequestParam String nickname,
            @RequestParam Long authId) {
        meetingService.requestJoinMeeting(invitationCode, nickname, authId);
        return ResponseEntity.ok().build();
    }

    
} 