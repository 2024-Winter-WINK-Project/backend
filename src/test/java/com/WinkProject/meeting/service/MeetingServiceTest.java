package com.winkproject.meeting.service;

import com.winkproject.common.fixture.MeetingTestFixture;
import com.winkproject.meeting.domain.Meeting;
import com.winkproject.meeting.dto.request.MeetingCreateRequest;
import com.winkproject.meeting.dto.request.MeetingUpdateRequest;
import com.winkproject.meeting.dto.response.MeetingResponse;
import com.winkproject.meeting.repository.MeetingRepository;
import com.winkproject.member.domain.Auth;
import com.winkproject.member.domain.Member;
import com.winkproject.member.domain.MemberRole;
import com.winkproject.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    @InjectMocks
    private MeetingService meetingService;

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private MemberRepository memberRepository;

    private Meeting testMeeting;
    private Member testMember;
    private Auth testAuth;

    @BeforeEach
    void setUp() {
        // Auth 설정
        testAuth = new Auth();
        testAuth.setId(1L);
        testAuth.setSocialId("test123");
        testAuth.setSocialType("KAKAO");
        testAuth.setNickname("테스트 유저");
        testAuth.setProfileImage("test.jpg");

        // Member 설정
        testMember = new Member();
        testMember.setMemberId(1L);
        testMember.setAuth(testAuth);
        testMember.setRole(MemberRole.OWNER);
        testMember.setNickname(testAuth.getNickname());
        testMember.setDefaultProfileImage(testAuth.getProfileImage());

        // Meeting 설정
        testMeeting = MeetingTestFixture.createMeeting("테스트 모임");
        testMeeting.setId(1L);
        testMeeting.getMembers().add(testMember);
        testMember.setMeeting(testMeeting);
    }

    @Test
    @DisplayName("모임 생성 - 성공")
    void createMeeting_Success() {
        // given
        MeetingCreateRequest request = MeetingTestFixture.createMeetingRequest("새로운 모임");
        given(meetingRepository.save(any(Meeting.class))).willReturn(testMeeting);
        given(memberRepository.save(any(Member.class))).willReturn(testMember);

        // when
        MeetingResponse response = meetingService.createMeeting(request, testAuth.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(testMeeting.getName());
        verify(meetingRepository).save(any(Meeting.class));
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("모임 생성 - 실패 (과거 시간)")
    void createMeeting_Fail_PastTime() {
        // given
        MeetingCreateRequest request = MeetingTestFixture.createMeetingRequest("새로운 모임");
        request.setStartTime(LocalDateTime.now().minusDays(1));

        // when & then
        assertThatThrownBy(() -> meetingService.createMeeting(request, testAuth.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("시작 시간은 현재 시간 이후여야 합니다.");
    }

    @Test
    @DisplayName("모임 수정 - 성공")
    void updateMeeting_Success() {
        // given
        given(meetingRepository.findById(1L)).willReturn(Optional.of(testMeeting));
        MeetingUpdateRequest request = MeetingTestFixture.createMeetingUpdateRequest("수정된 모임");

        // when
        MeetingResponse response = meetingService.updateMeeting(1L, request, testMember.getMemberId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("수정된 모임");
        assertThat(response.getDescription()).isEqualTo("수정된 모임입니다.");
    }

    @Test
    @DisplayName("모임 수정 - 실패 (권한 없음)")
    void updateMeeting_Fail_NotOwner() {
        // given
        Auth otherAuth = new Auth();
        otherAuth.setId(2L);
        Member otherMember = new Member();
        otherMember.setMemberId(2L);
        otherMember.setAuth(otherAuth);
        otherMember.setRole(MemberRole.MEMBER);

        given(meetingRepository.findById(1L)).willReturn(Optional.of(testMeeting));

        MeetingUpdateRequest request = MeetingTestFixture.createMeetingUpdateRequest("수정된 모임");

        // when & then
        assertThatThrownBy(() -> meetingService.updateMeeting(1L, request, otherMember.getMemberId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모임장만 이 작업을 수행할 수 있습니다.");
    }

    @Test
    @DisplayName("최근 모임 조회 - 성공")
    void getLatestMeetings_Success() {
        // given
        given(meetingRepository.findLatestMeetingDTOs(any(Long.class), any(PageRequest.class)))
                .willReturn(List.of(MeetingResponse.from(testMeeting)));

        // when
        List<MeetingResponse> responses = meetingService.getLatestMeetings(5, testAuth.getId());

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo(testMeeting.getName());
    }

    @Test
    @DisplayName("모임 삭제 - 성공")
    void deleteMeeting_Success() {
        // given
        given(meetingRepository.findById(1L)).willReturn(Optional.of(testMeeting));

        // when
        meetingService.deleteMeeting(1L, testMember.getMemberId());

        // then
        verify(meetingRepository).delete(testMeeting);
    }
} 