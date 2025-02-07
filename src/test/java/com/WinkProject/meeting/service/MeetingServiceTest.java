package com.WinkProject.meeting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.WinkProject.meeting.domain.Meeting;
import com.WinkProject.meeting.domain.Place;
import com.WinkProject.meeting.domain.Settlement;
import com.WinkProject.meeting.dto.request.MeetingCreateRequest;
import com.WinkProject.meeting.dto.request.MeetingUpdateRequest;
import com.WinkProject.meeting.dto.response.MeetingBriefResponse;
import com.WinkProject.meeting.dto.response.MeetingResponse;
import com.WinkProject.meeting.repository.MeetingRepository;
import com.WinkProject.meeting.repository.SettlementRepository;
import com.WinkProject.auth.schema.Auth;
import com.WinkProject.member.domain.Member;
import com.WinkProject.auth.repository.AuthRepository;
import com.WinkProject.invitation.domain.Invitation;
import com.WinkProject.invitation.repository.InvitationRepository;
import com.WinkProject.member.repository.MemberRepository;
import com.WinkProject.invitation.dto.response.InvitationResponse;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    @InjectMocks
    private MeetingService meetingService;

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private AuthRepository authRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private InvitationRepository invitationRepository;

    @Nested
    @DisplayName("모임 조회 테스트")
    class GetMeetingsTest {

        @Test
        @DisplayName("사용자의 모임 목록 조회")
        void getMeetingsByAuthId() {
            // given
            Long authId = 1L;
            List<Meeting> meetings = new ArrayList<>();
            
            // 더미 데이터 생성
            for (int i = 1; i <= 3; i++) {
                Meeting meeting = new Meeting();
                meeting.setId((long) i);
                meeting.setName("테스트 모임 " + i);
                meeting.setStartTime(LocalDateTime.now().plusDays(i));
                meeting.setEndTime(LocalDateTime.now().plusDays(i).plusHours(2));
                meeting.setOwnerId(i == 1 ? authId : authId + i); // 첫 번째 모임만 해당 사용자가 모임장

                // 장소 정보 설정
                Place place = new Place();
                place.setId((long) i);
                place.setName("테스트 장소 " + i);
                place.setAddress("서울시 강남구 테스트동 " + i);
                place.setLatitude(37.5 + (i * 0.1));
                place.setLongitude(127.0 + (i * 0.1));
                meeting.setPlace(place);

                meetings.add(meeting);
            }

            when(meetingRepository.findMeetingsByAuthId(authId)).thenReturn(meetings);

            // when
            List<MeetingBriefResponse> responses = meetingService.getMeetingsByAuthId(authId);

            // then
            assertThat(responses).hasSize(3);
            assertThat(responses.get(0).getName()).isEqualTo("테스트 모임 1");
            assertThat(responses.get(0).isOwner()).isTrue();
            assertThat(responses.get(1).getName()).isEqualTo("테스트 모임 2");
            assertThat(responses.get(1).isOwner()).isFalse();
            
            // 각 모임의 기본 정보 확인
            for (int i = 0; i < responses.size(); i++) {
                MeetingBriefResponse response = responses.get(i);
                assertThat(response.getId()).isEqualTo(i + 1);
                assertThat(response.getStartTime()).isNotNull();
                assertThat(response.getEndTime()).isNotNull();
            }

            // verify
            verify(meetingRepository).findMeetingsByAuthId(authId);
        }

        @Test
        @DisplayName("최근 모임 N개 조회")
        void getLatestMeetings() {
            // given
            Long authId = 1L;
            int limit = 3;
            List<Meeting> meetings = new ArrayList<>();
            
            // 더미 데이터 생성 (5개 생성하여 limit 동작 확인)
            for (int i = 1; i <= 5; i++) {
                Meeting meeting = new Meeting();
                meeting.setId((long) i);
                meeting.setName("테스트 모임 " + i);
                meeting.setStartTime(LocalDateTime.now().plusDays(i));
                meeting.setEndTime(LocalDateTime.now().plusDays(i).plusHours(2));
                meeting.setOwnerId(i == 1 ? authId : authId + i);

                // 장소 정보 설정
                Place place = new Place();
                place.setId((long) i);
                place.setName("테스트 장소 " + i);
                place.setAddress("서울시 강남구 테스트동 " + i);
                place.setLatitude(37.5 + (i * 0.1));
                place.setLongitude(127.0 + (i * 0.1));
                meeting.setPlace(place);

                meetings.add(meeting);
            }

            when(meetingRepository.findLatestMeetingsByAuthId(authId, limit)).thenReturn(meetings.subList(0, limit));

            // when
            List<MeetingBriefResponse> responses = meetingService.getLatestMeetings(limit, authId);

            // then
            assertThat(responses).hasSize(limit);
            assertThat(responses.get(0).getName()).isEqualTo("테스트 모임 1");
            assertThat(responses.get(0).isOwner()).isTrue();
            assertThat(responses.get(1).getName()).isEqualTo("테스트 모임 2");
            assertThat(responses.get(1).isOwner()).isFalse();
            
            // 각 모임의 기본 정보 확인
            for (int i = 0; i < responses.size(); i++) {
                MeetingBriefResponse response = responses.get(i);
                assertThat(response.getId()).isEqualTo(i + 1);
                assertThat(response.getStartTime()).isNotNull();
                assertThat(response.getEndTime()).isNotNull();
            }

            // verify
            verify(meetingRepository).findLatestMeetingsByAuthId(authId, limit);
        }
    }

    @Nested
    @DisplayName("모임 생성 테스트")
    class CreateMeetingTest {
        
        private MeetingCreateRequest request;
        private Auth testAuth;
        private String nickname;
        private Long authId;

        @BeforeEach
        void setUp() {
            // 테스트 데이터 설정
            request = new MeetingCreateRequest();
            request.setName("테스트 모임");
            request.setDescription("테스트 모임입니다.");
            request.setStartTime(LocalDateTime.now().plusDays(1));
            request.setEndTime(LocalDateTime.now().plusDays(2));

            // Place 정보 설정
            MeetingCreateRequest.PlaceRequest placeRequest = new MeetingCreateRequest.PlaceRequest();
            placeRequest.setName("테스트 장소");
            placeRequest.setAddress("서울시 강남구");
            placeRequest.setLatitude(37.5);
            placeRequest.setLongitude(127.0);
            request.setPlace(placeRequest);

            testAuth = Auth.builder()
                    .id(1L)
                    .socialId("12345")
                    .socialType("KAKAO")
                    .nickname("테스트 유저")
                    .profileImage("test_profile.jpg")
                    .build();
            nickname = "모임장";
            authId = 1L;

            // Mock 설정
            when(authRepository.findById(authId)).thenReturn(Optional.of(testAuth));
            when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> {
                Meeting meeting = invocation.getArgument(0);
                meeting.setId(1L);
                return meeting;
            });
        }

        @Test
        @DisplayName("정산 정보 없이 모임 생성")
        void createMeetingWithoutSettlement() {
            // when
            MeetingResponse response = meetingService.createMeeting(request, authId, nickname);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("테스트 모임");
            assertThat(response.getDescription()).isEqualTo("테스트 모임입니다.");
            assertThat(response.getPlace()).isNotNull();
            assertThat(response.getPlace().getName()).isEqualTo("테스트 장소");
            assertThat(response.getPlace().getAddress()).isEqualTo("서울시 강남구");
            assertThat(response.getPlace().getLatitude()).isEqualTo(37.5);
            assertThat(response.getPlace().getLongitude()).isEqualTo(127.0);

            verify(authRepository).findById(authId);
            verify(meetingRepository).save(any(Meeting.class));
            verify(settlementRepository, never()).save(any(Settlement.class));
        }
    }

    @Nested
    @DisplayName("모임 수정 테스트")
    class UpdateMeetingTest {
        private Meeting testMeeting;
        private Long ownerAuthId = 1L;
        private Long nonOwnerAuthId = 2L;
        private Member ownerMember;

        @BeforeEach
        void setUp() {
            // 모임장 Auth 설정
            Auth ownerAuth = new Auth();
            ownerAuth.setId(ownerAuthId);
            ownerAuth.setNickname("모임장");
            
            // 모임 설정
            testMeeting = new Meeting();
            testMeeting.setId(1L);
            testMeeting.setName("테스트 모임");
            testMeeting.setDescription("테스트 모임 설명");
            testMeeting.setStartTime(LocalDateTime.now().plusDays(1));
            testMeeting.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
            testMeeting.setOwnerId(ownerAuthId);

            // 모임장 멤버 설정
            ownerMember = Member.createMember(testMeeting, ownerAuth, "모임장");
            ownerMember.setId(1L);
            testMeeting.getMembers().add(ownerMember);
        }

        @Test
        @DisplayName("모임 정보 정상 수정")
        void updateMeetingSuccess() {
            // given
            when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
            when(meetingRepository.save(any(Meeting.class))).thenReturn(testMeeting);
            
            MeetingUpdateRequest request = new MeetingUpdateRequest();
            request.setName("수정된 모임명");
            request.setDescription("수정된 설명");

            // when
            MeetingResponse response = meetingService.updateMeeting(1L, request, ownerAuthId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("수정된 모임명");
            assertThat(response.getDescription()).isEqualTo("수정된 설명");
            verify(meetingRepository).findById(1L);
            verify(meetingRepository).save(testMeeting);
        }

        @Test
        @DisplayName("모임장이 아닌 사용자가 수정 시도")
        void updateMeetingByNonOwner() {
            // given
            when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
            
            MeetingUpdateRequest request = new MeetingUpdateRequest();
            request.setName("수정된 모임명");

            // when & then
            assertThatThrownBy(() -> meetingService.updateMeeting(1L, request, nonOwnerAuthId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모임장만 모임 정보를 수정할 수 있습니다.");
            
            verify(meetingRepository).findById(1L);
            verify(meetingRepository, never()).save(any(Meeting.class));
        }

        @Test
        @DisplayName("존재하지 않는 모임 수정 시도")
        void updateNonExistentMeeting() {
            // given
            when(meetingRepository.findById(999L)).thenReturn(Optional.empty());
            MeetingUpdateRequest request = new MeetingUpdateRequest();
            request.setName("수정된 모임명");

            // when & then
            assertThatThrownBy(() -> meetingService.updateMeeting(999L, request, ownerAuthId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모임을 찾을 수 없습니다.");
            
            verify(meetingRepository).findById(999L);
            verify(meetingRepository, never()).save(any(Meeting.class));
        }

        @Test
        @DisplayName("일부 필드만 수정")
        void updatePartialFields() {
            // given
            when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
            when(meetingRepository.save(any(Meeting.class))).thenReturn(testMeeting);
            
            MeetingUpdateRequest request = new MeetingUpdateRequest();
            request.setName("수정된 모임명");
            // description은 수정하지 않음

            // when
            MeetingResponse response = meetingService.updateMeeting(1L, request, ownerAuthId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("수정된 모임명");
            assertThat(response.getDescription()).isEqualTo("테스트 모임 설명"); // 기존 값 유지
            verify(meetingRepository).findById(1L);
            verify(meetingRepository).save(testMeeting);
        }
    }

    @Nested
    @DisplayName("모임 삭제 테스트")
    class DeleteMeetingTest {
        private Meeting testMeeting;
        private Long ownerAuthId = 1L;
        private Long nonOwnerAuthId = 2L;

        @BeforeEach
        void setUp() {
            testMeeting = new Meeting();
            testMeeting.setId(1L);
            testMeeting.setOwnerId(ownerAuthId);
        }

        @Test
        @DisplayName("모임장은 모임을 삭제할 수 있다")
        void deleteMeetingByOwnerSuccess() {
            // given
            when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
            
            // when
            meetingService.deleteMeeting(1L, ownerAuthId);

            // then
            verify(meetingRepository).findById(1L);
            verify(meetingRepository).delete(testMeeting);
        }

        @Test
        @DisplayName("모임장이 아닌 사용자는 모임을 삭제할 수 없다")
        void deleteMeetingByNonOwnerFail() {
            // given
            when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
            
            // when & then
            assertThatThrownBy(() -> meetingService.deleteMeeting(1L, nonOwnerAuthId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모임장만 모임을 삭제할 수 있습니다.");

            verify(meetingRepository).findById(1L);
            verify(meetingRepository, never()).delete(any(Meeting.class));
        }

        @Test
        @DisplayName("존재하지 않는 모임을 삭제하려고 하면 예외가 발생한다")
        void deleteMeetingNotFound() {
            // given
            when(meetingRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> meetingService.deleteMeeting(999L, ownerAuthId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모임을 찾을 수 없습니다.");

            verify(meetingRepository).findById(999L);
            verify(meetingRepository, never()).delete(any(Meeting.class));
        }
    }

    @Nested
    @DisplayName("모임 탈퇴 테스트")
    class LeaveMeetingTest {
        private Meeting testMeeting;
        private Member ownerMember;
        private Member normalMember;
        private Long ownerAuthId = 1L;
        private Long normalAuthId = 2L;

        @BeforeEach
        void setUp() {
            // 모임 설정
            testMeeting = new Meeting();
            testMeeting.setId(1L);
            testMeeting.setOwnerId(ownerAuthId);

            // 모임장 설정
            Auth ownerAuth = new Auth();
            ownerAuth.setId(ownerAuthId);
            ownerMember = Member.createMember(testMeeting, ownerAuth, "모임장");
            testMeeting.getMembers().add(ownerMember);

            // 일반 멤버 설정
            Auth normalAuth = new Auth();
            normalAuth.setId(normalAuthId);
            normalMember = Member.createMember(testMeeting, normalAuth, "일반 멤버");
            testMeeting.getMembers().add(normalMember);
        }

        @Test
        @DisplayName("일반 멤버는 모임을 탈퇴할 수 있다")
        void leaveMeetingSuccess() {
            // given
            when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
            
            // when
            meetingService.leaveMeeting(1L, normalAuthId);

            // then
            assertThat(normalMember.isWithdrawn()).isTrue();
            verify(meetingRepository).findById(1L);
        }

        @Test
        @DisplayName("모임장은 모임을 탈퇴할 수 없다")
        void leaveMeetingByOwnerFail() {
            // given
            when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
            
            // when & then
            assertThatThrownBy(() -> meetingService.leaveMeeting(1L, ownerAuthId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모임장은 탈퇴할 수 없습니다. 먼저 모임장 위임이 필요합니다.");

            verify(meetingRepository).findById(1L);
        }

        @Test
        @DisplayName("모임의 멤버가 아닌 사용자는 탈퇴할 수 없다")
        void leaveMeetingByNonMemberFail() {
            // given
            when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
            
            // when & then
            assertThatThrownBy(() -> meetingService.leaveMeeting(1L, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모임의 멤버가 아닙니다.");

            verify(meetingRepository).findById(1L);
        }

        @Test
        @DisplayName("존재하지 않는 모임에서 탈퇴할 수 없다")
        void leaveMeetingFromNonExistentMeeting() {
            // given
            when(meetingRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> meetingService.leaveMeeting(999L, normalAuthId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모임을 찾을 수 없습니다.");

            verify(meetingRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("모임장 위임 테스트")
    class DelegateOwnerTest {
        private Meeting testMeeting;
        private Member ownerMember;
        private Member normalMember;
        private Member withdrawnMember;
        private Long ownerAuthId = 1L;
        private Long normalAuthId = 2L;
        private Long withdrawnAuthId = 3L;

        @BeforeEach
        void setUp() {
            // 모임 설정
            testMeeting = new Meeting();
            testMeeting.setId(1L);
            testMeeting.setOwnerId(ownerAuthId);

            // 모임장 설정
            Auth ownerAuth = new Auth();
            ownerAuth.setId(ownerAuthId);
            ownerMember = Member.createMember(testMeeting, ownerAuth, "모임장");
            testMeeting.getMembers().add(ownerMember);

            // 일반 멤버 설정
            Auth normalAuth = new Auth();
            normalAuth.setId(normalAuthId);
            normalMember = Member.createMember(testMeeting, normalAuth, "일반 멤버");
            testMeeting.getMembers().add(normalMember);

            // 탈퇴한 멤버 설정
            Auth withdrawnAuth = new Auth();
            withdrawnAuth.setId(withdrawnAuthId);
            withdrawnMember = Member.createMember(testMeeting, withdrawnAuth, "탈퇴한 멤버");
            withdrawnMember.setWithdrawn(true);
            testMeeting.getMembers().add(withdrawnMember);
        }

        @Test
        @DisplayName("모임장 권한 정상 위임")
        void delegateOwnerSuccess() {
            // given
            when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
            when(meetingRepository.save(any(Meeting.class))).thenReturn(testMeeting);

            // when
            meetingService.delegateOwner(1L, ownerAuthId, normalAuthId);

            // then
            assertThat(testMeeting.getOwnerId()).isEqualTo(normalAuthId);
            verify(meetingRepository).findById(1L);
            verify(meetingRepository).save(testMeeting);
        }

        @Test
        @DisplayName("모임장이 아닌 사용자가 위임 시도")
        void delegateOwnerByNonOwnerFail() {
            // given
            when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));

            // when & then
            assertThatThrownBy(() -> meetingService.delegateOwner(1L, normalAuthId, withdrawnAuthId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모임장만 권한을 위임할 수 있습니다.");

            verify(meetingRepository).findById(1L);
            verify(meetingRepository, never()).save(any(Meeting.class));
        }

        @Test
        @DisplayName("모임에 속하지 않은 멤버에게 위임 시도")
        void delegateOwnerToNonMemberFail() {
            // given
            when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
            Long nonMemberAuthId = 999L;

            // when & then
            assertThatThrownBy(() -> meetingService.delegateOwner(1L, ownerAuthId, nonMemberAuthId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("위임할 멤버가 모임에 속해있지 않습니다.");

            verify(meetingRepository).findById(1L);
            verify(meetingRepository, never()).save(any(Meeting.class));
        }

        @Test
        @DisplayName("탈퇴한 멤버에게 위임 시도")
        void delegateOwnerToWithdrawnMemberFail() {
            // given
            when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));

            // when & then
            assertThatThrownBy(() -> meetingService.delegateOwner(1L, ownerAuthId, withdrawnAuthId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("탈퇴한 멤버에게 모임장을 위임할 수 없습니다.");

            verify(meetingRepository).findById(1L);
            verify(meetingRepository, never()).save(any(Meeting.class));
        }

        @Test
        @DisplayName("존재하지 않는 모임의 모임장 위임 시도")
        void delegateOwnerNonExistentMeetingFail() {
            // given
            when(meetingRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> meetingService.delegateOwner(999L, ownerAuthId, normalAuthId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모임을 찾을 수 없습니다.");

            verify(meetingRepository).findById(999L);
            verify(meetingRepository, never()).save(any(Meeting.class));
        }
    }

    @Nested
    @DisplayName("모임 상세 조회 테스트")
    class GetMeetingDetailTest {
        private Meeting meeting;
        private Member ownerMember;
        private Member normalMember;
        private Place place;

        @BeforeEach
        void setUp() {
            // 모임 생성
            meeting = new Meeting();
            meeting.setId(1L);
            meeting.setName("테스트 모임");
            meeting.setDescription("테스트 모임입니다.");
            meeting.setStartTime(LocalDateTime.now().plusDays(1));
            meeting.setEndTime(LocalDateTime.now().plusDays(2));
            meeting.setOwnerId(1L);

            // 장소 설정
            place = new Place();
            place.setName("테스트 장소");
            place.setAddress("서울시 강남구");
            place.setLatitude(37.5);
            place.setLongitude(127.0);
            meeting.setPlace(place);

            // 모임장 생성
            Auth ownerAuth = Auth.builder()
                    .id(1L)
                    .socialId("12345")
                    .socialType("KAKAO")
                    .nickname("모임장")
                    .profileImage("owner_profile.jpg")
                    .build();

            ownerMember = Member.createMember(meeting, ownerAuth, "모임장");
            meeting.getMembers().add(ownerMember);

            // 일반 멤버 생성
            Auth normalAuth = Auth.builder()
                    .id(2L)
                    .socialId("67890")
                    .socialType("KAKAO")
                    .nickname("일반 멤버")
                    .profileImage("member_profile.jpg")
                    .build();

            normalMember = Member.createMember(meeting, normalAuth, "일반 멤버");
            meeting.getMembers().add(normalMember);
        }

        @Test
        @DisplayName("모임 상세 정보 정상 조회")
        void getMeetingDetailSuccess() {
            // given
            when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));

            // when
            MeetingResponse response = meetingService.getMeetingDetail(1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("테스트 모임");
            assertThat(response.getDescription()).isEqualTo("테스트 모임입니다.");
            
            // 장소 정보 확인
            assertThat(response.getPlace()).isNotNull();
            assertThat(response.getPlace().getName()).isEqualTo("테스트 장소");
            assertThat(response.getPlace().getAddress()).isEqualTo("서울시 강남구");
            assertThat(response.getPlace().getLatitude()).isEqualTo(37.5);
            assertThat(response.getPlace().getLongitude()).isEqualTo(127.0);
            
            // 모임장 정보 확인
            assertThat(response.getOwner()).isNotNull();
            assertThat(response.getOwner().getNickname()).isEqualTo("모임장");
            assertThat(response.getOwner().getProfileImage()).isEqualTo("owner_profile.jpg");
            
            // 멤버 목록 확인
            assertThat(response.getMembers()).hasSize(2);
            assertThat(response.getMembers().get(0).getNickname()).isEqualTo("모임장");
            assertThat(response.getMembers().get(1).getNickname()).isEqualTo("일반 멤버");
        }

        @Test
        @DisplayName("정산 정보가 있는 모임 조회")
        void getMeetingDetailWithSettlement() {
            // given
            Settlement settlement = new Settlement(meeting);
            settlement.setKakaoPayString("카카오페이");
            settlement.setTossPayString("토스");
            settlement.setAccountNumber("123-456-789");
            meeting.setSettlement(settlement);
            when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));

            // when
            MeetingResponse response = meetingService.getMeetingDetail(1L);

            // then
            assertThat(response.getSettlement()).isNotNull();
            assertThat(response.getSettlement().getKakaoPayString()).isEqualTo("카카오페이");
            assertThat(response.getSettlement().getTossPayString()).isEqualTo("토스");
            assertThat(response.getSettlement().getAccountNumber()).isEqualTo("123-456-789");
        }

        @Test
        @DisplayName("존재하지 않는 모임 조회")
        void getMeetingDetailNotFound() {
            // given
            when(meetingRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> meetingService.getMeetingDetail(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("모임을 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("모임 멤버 강제 퇴장 테스트")
    class KickMemberTest {
        private Meeting testMeeting;
        private Member ownerMember;
        private Member normalMember;
        private Member withdrawnMember;
        private Long ownerAuthId = 1L;
        private Long normalAuthId = 2L;
        private Long withdrawnAuthId = 3L;

        @BeforeEach
        void setUp() {
            // 모임 설정
            testMeeting = new Meeting();
            testMeeting.setId(1L);
            testMeeting.setOwnerId(ownerAuthId);

            // 모임장 설정
            Auth ownerAuth = new Auth();
            ownerAuth.setId(ownerAuthId);
            ownerMember = Member.createMember(testMeeting, ownerAuth, "모임장");
            testMeeting.getMembers().add(ownerMember);

            // 일반 멤버 설정
            Auth normalAuth = new Auth();
            normalAuth.setId(normalAuthId);
            normalMember = Member.createMember(testMeeting, normalAuth, "일반 멤버");
            testMeeting.getMembers().add(normalMember);

            // 탈퇴한 멤버 설정
            Auth withdrawnAuth = new Auth();
            withdrawnAuth.setId(withdrawnAuthId);
            withdrawnMember = Member.createMember(testMeeting, withdrawnAuth, "탈퇴한 멤버");
            withdrawnMember.setWithdrawn(true);
            testMeeting.getMembers().add(withdrawnMember);
        }

        @Test
        @DisplayName("모임장이 일반 멤버를 정상적으로 강제 퇴장")
        void kickMemberSuccess() {
            // given
            when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
            when(meetingRepository.save(any(Meeting.class))).thenReturn(testMeeting);

            // when
            meetingService.kickMember(1L, normalAuthId, ownerAuthId);

            // then
            assertThat(normalMember.isWithdrawn()).isTrue();
            verify(meetingRepository).findById(1L);
            verify(meetingRepository).save(testMeeting);
        }

        @Test
        @DisplayName("모임장이 아닌 사용자가 강제 퇴장 시도")
        void kickMemberByNonOwnerFail() {
            // given
            when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));

            // when & then
            assertThatThrownBy(() -> meetingService.kickMember(1L, withdrawnAuthId, normalAuthId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모임장만 멤버를 강제 퇴장시킬 수 있습니다.");

            verify(meetingRepository).findById(1L);
            verify(meetingRepository, never()).save(any(Meeting.class));
        }

        @Test
        @DisplayName("모임에 속하지 않은 멤버 강제 퇴장 시도")
        void kickNonMemberFail() {
            // given
            when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
            Long nonMemberAuthId = 999L;

            // when & then
            assertThatThrownBy(() -> meetingService.kickMember(1L, nonMemberAuthId, ownerAuthId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("퇴장시킬 멤버가 모임에 속해있지 않습니다.");

            verify(meetingRepository).findById(1L);
            verify(meetingRepository, never()).save(any(Meeting.class));
        }

        @Test
        @DisplayName("이미 탈퇴한 멤버 강제 퇴장 시도")
        void kickWithdrawnMemberFail() {
            // given
            when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));

            // when & then
            assertThatThrownBy(() -> meetingService.kickMember(1L, withdrawnAuthId, ownerAuthId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 탈퇴한 멤버입니다.");

            verify(meetingRepository).findById(1L);
            verify(meetingRepository, never()).save(any(Meeting.class));
        }

        @Test
        @DisplayName("모임장 강제 퇴장 시도")
        void kickOwnerFail() {
            // given
            when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));

            // when & then
            assertThatThrownBy(() -> meetingService.kickMember(1L, ownerAuthId, ownerAuthId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모임장은 강제 퇴장시킬 수 없습니다.");

            verify(meetingRepository).findById(1L);
            verify(meetingRepository, never()).save(any(Meeting.class));
        }

        @Test
        @DisplayName("존재하지 않는 모임에서 강제 퇴장 시도")
        void kickMemberFromNonExistentMeetingFail() {
            // given
            when(meetingRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> meetingService.kickMember(999L, normalAuthId, ownerAuthId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모임을 찾을 수 없습니다.");

            verify(meetingRepository).findById(999L);
            verify(meetingRepository, never()).save(any(Meeting.class));
        }
    }

    @Nested
    @DisplayName("초대 테스트")
    class InvitationTest {
        private Meeting meeting;
        private Auth ownerAuth;
        private Auth memberAuth;
        private Member owner;
        private String validInviteCode;
        private Invitation invitation;

        @BeforeEach
        void setUp() {
            // 모임 및 모임장 설정
            ownerAuth = new Auth();
            ownerAuth.setId(1L);
            
            meeting = new Meeting();
            meeting.setId(1L);
            meeting.setOwnerId(ownerAuth.getId());
            
            owner = Member.createMember(meeting, ownerAuth, "owner");
            owner.setId(1L);  // Member ID 설정
            meeting.getMembers().add(owner);

            // 일반 멤버 설정
            memberAuth = new Auth();
            memberAuth.setId(2L);

            // 초대장 설정
            validInviteCode = UUID.randomUUID().toString();
            invitation = new Invitation();
            invitation.setId(1L);
            invitation.setInviteCode(validInviteCode);
            invitation.setMeeting(meeting);
            invitation.setExpiresAt(LocalDateTime.now().plusDays(1));

            // Mock 설정
            lenient().when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        }

        @Test
        void createInvitation_Success() {
            // given
            when(invitationRepository.existsByInviteCode(any())).thenReturn(false);
            when(invitationRepository.save(any(Invitation.class))).thenAnswer(invocation -> {
                Invitation savedInvitation = invocation.getArgument(0);
                savedInvitation.setId(1L);
                savedInvitation.setMeeting(meeting);
                savedInvitation.setInviteCode(UUID.randomUUID().toString());
                savedInvitation.setExpiresAt(LocalDateTime.now().plusDays(1));
                return savedInvitation;
            });

            // when
            InvitationResponse response = meetingService.createInvitation(1L, ownerAuth.getId());

            // then
            assertNotNull(response);
            assertNotNull(response.getInviteCode());
            verify(invitationRepository).save(any(Invitation.class));
        }

        @Test
        void requestJoinMeeting_Success() {
            // given
            String newNickname = "newMember";
            when(invitationRepository.findByInviteCode(validInviteCode)).thenReturn(Optional.of(invitation));
            when(memberRepository.existsByMeetingIdAndNicknameAndIsWithdrawnFalse(1L, newNickname)).thenReturn(false);
            when(authRepository.findById(memberAuth.getId())).thenReturn(Optional.of(memberAuth));

            // when
            assertDoesNotThrow(() -> 
                meetingService.requestJoinMeeting(validInviteCode, newNickname, memberAuth.getId()));

            // then
            verify(meetingRepository).save(any(Meeting.class));
        }

        @Test
        void requestJoinMeeting_DuplicateNickname_Fail() {
            // given
            String existingNickname = "existingMember";
            when(invitationRepository.findByInviteCode(validInviteCode)).thenReturn(Optional.of(invitation));
            when(memberRepository.existsByMeetingIdAndNicknameAndIsWithdrawnFalse(1L, existingNickname)).thenReturn(true);

            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                meetingService.requestJoinMeeting(validInviteCode, existingNickname, memberAuth.getId()));
        }

        @Test
        void requestJoinMeeting_InvalidCode_Fail() {
            // given
            String invalidCode = "invalid-code";
            when(invitationRepository.findByInviteCode(invalidCode)).thenReturn(Optional.empty());

            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                meetingService.requestJoinMeeting(invalidCode, "newMember", memberAuth.getId()));
        }

        @Test
        void requestJoinMeeting_ExpiredCode_Fail() {
            // given
            invitation.setExpiresAt(LocalDateTime.now().minusDays(1));
            when(invitationRepository.findByInviteCode(validInviteCode)).thenReturn(Optional.of(invitation));

            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                meetingService.requestJoinMeeting(validInviteCode, "newMember", memberAuth.getId()));
        }
    }
} 