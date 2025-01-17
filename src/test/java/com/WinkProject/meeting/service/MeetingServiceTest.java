package com.WinkProject.meeting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import com.WinkProject.meeting.dto.request.MeetingCreateRequest;
import com.WinkProject.meeting.dto.request.MeetingUpdateRequest;
import com.WinkProject.meeting.dto.response.MeetingBriefResponse;
import com.WinkProject.meeting.dto.response.MeetingResponse;
import com.WinkProject.meeting.dto.response.MemberProfileResponse;
import com.WinkProject.meeting.repository.MeetingRepository;
import com.WinkProject.meeting.repository.SettlementRepository;
import com.WinkProject.member.domain.Auth;
import com.WinkProject.member.domain.Member;
import com.WinkProject.member.repository.AuthRepository;
import com.WinkProject.member.repository.MemberRepository;

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

    @Nested
    @DisplayName("모임 조회 테스트")
    class GetMeetingsTest {

        @Test
        @DisplayName("사용자의 모임 목록 조회")
        void getMeetingsByUserId() {
            // given
            Long userId = 1L;
            List<Meeting> meetings = new ArrayList<>();
            
            // 더미 데이터 생성
            for (int i = 1; i <= 3; i++) {
                Meeting meeting = new Meeting();
                meeting.setId((long) i);
                meeting.setName("테스트 모임 " + i);
                meeting.setStartTime(LocalDateTime.now().plusDays(i));
                meeting.setEndTime(LocalDateTime.now().plusDays(i).plusHours(2));
                meeting.setOwnerId(i == 1 ? userId : userId + i); // 첫 번째 모임만 해당 사용자가 모임장

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

            when(meetingRepository.findMeetingsByUserId(userId)).thenReturn(meetings);

            // when
            List<MeetingBriefResponse> responses = meetingService.getMeetingsByUserId(userId);

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
            verify(meetingRepository).findMeetingsByUserId(userId);
        }

        @Test
        @DisplayName("최근 모임 N개 조회")
        void getLatestMeetings() {
            // given
            Long userId = 1L;
            int limit = 3;
            List<Meeting> meetings = new ArrayList<>();
            
            // 더미 데이터 생성 (5개 생성하여 limit 동작 확인)
            for (int i = 1; i <= 5; i++) {
                Meeting meeting = new Meeting();
                meeting.setId((long) i);
                meeting.setName("테스트 모임 " + i);
                meeting.setStartTime(LocalDateTime.now().plusDays(i));
                meeting.setEndTime(LocalDateTime.now().plusDays(i).plusHours(2));
                meeting.setOwnerId(i == 1 ? userId : userId + i);

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

            when(meetingRepository.findLatestMeetings(userId, limit)).thenReturn(meetings.subList(0, limit));

            // when
            List<MeetingBriefResponse> responses = meetingService.getLatestMeetings(limit, userId);

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
            verify(meetingRepository).findLatestMeetings(userId, limit);
        }
    }

    @Nested
    @DisplayName("모임 생성 테스트")
    class CreateMeetingTest {
        
        private MeetingCreateRequest request;
        private Auth testAuth;

        @BeforeEach
        void setUp() {
            // 테스트용 Auth 생성
            testAuth = new Auth();
            testAuth.setId(1L);
            testAuth.setSocialId("test123");
            testAuth.setSocialType("KAKAO");
            testAuth.setNickname("테스트 유저");
            testAuth.setProfileImage("test_profile.jpg");

            when(authRepository.findById(1L)).thenReturn(java.util.Optional.of(testAuth));

            // MeetingCreateRequest 설정
            request = new MeetingCreateRequest();
            request.setName("테스트 모임");
            request.setStartTime(LocalDateTime.now().plusDays(1));
            request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

            // Place 정보 설정
            MeetingCreateRequest.PlaceRequest placeRequest = new MeetingCreateRequest.PlaceRequest();
            placeRequest.setName("테스트 장소");
            placeRequest.setAddress("서울시 강남구");
            placeRequest.setLatitude(37.5);
            placeRequest.setLongitude(127.0);
            request.setPlace(placeRequest);
        }

        @Test
        @DisplayName("정산 정보 없이 모임 생성")
        void createMeetingWithoutSettlement() {
            // given
            String nickname = "모임장 닉네임";
            when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> {
                Meeting meeting = invocation.getArgument(0);
                meeting.setId(1L);

                // 모임장 정보 설정
                Member owner = meeting.getMembers().get(0);  // createMeeting에서 추가한 모임장
                owner.setId(1L);  // Member ID 설정
                
                return meeting;
            });

            // when
            MeetingResponse response = meetingService.createMeeting(request, 1L, nickname);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("테스트 모임");
            assertThat(response.getPlace()).isNotNull();
            assertThat(response.getPlace().getName()).isEqualTo("테스트 장소");
            assertThat(response.getOwner()).isNotNull();
            assertThat(response.getOwner().getId()).isEqualTo(1L);
            assertThat(response.getOwner().getNickname()).isEqualTo(nickname);
            verify(meetingRepository).save(any(Meeting.class));
            verify(authRepository).findById(1L);
        }
    }

    @Nested
    @DisplayName("모임 수정 테스트")
    class UpdateMeetingTest {
        private Meeting testMeeting;
        private Long ownerId = 1L;
        private Long nonOwnerId = 2L;
        private Member ownerMember;

        @BeforeEach
        void setUp() {
            // 모임장 Auth 설정
            Auth ownerAuth = new Auth();
            ownerAuth.setId(ownerId);
            ownerAuth.setNickname("모임장");
            
            // 모임 설정
            testMeeting = new Meeting();
            testMeeting.setId(1L);
            testMeeting.setName("기존 모임");
            testMeeting.setDescription("기존 설명");
            testMeeting.setOwnerId(ownerId);
            testMeeting.setStartTime(LocalDateTime.now().plusDays(1));
            testMeeting.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

            // 장소 설정
            Place place = new Place();
            place.setName("기존 장소");
            place.setAddress("기존 주소");
            place.setLatitude(37.5);
            place.setLongitude(127.0);
            testMeeting.setPlace(place);

            // 모임장 멤버 설정
            ownerMember = Member.createMember(testMeeting, ownerAuth, "모임장");
            ownerMember.setId(ownerId);
            testMeeting.getMembers().add(ownerMember);

            // Mockito 설정
            lenient().when(meetingRepository.findById(1L)).thenReturn(java.util.Optional.of(testMeeting));
            lenient().when(meetingRepository.save(any(Meeting.class))).thenAnswer(i -> i.getArgument(0));
        }

        @Test
        @DisplayName("모임 정보 정상 수정")
        void updateMeetingSuccess() {
            // given
            MeetingUpdateRequest request = new MeetingUpdateRequest();
            request.setName("수정된 모임");
            request.setDescription("수정된 설명");
            
            MeetingUpdateRequest.PlaceRequest placeRequest = new MeetingUpdateRequest.PlaceRequest();
            placeRequest.setName("수정된 장소");
            request.setPlace(placeRequest);

            // when
            MeetingResponse response = meetingService.updateMeeting(1L, request, ownerId);

            // then
            assertThat(response.getName()).isEqualTo("수정된 모임");
            assertThat(response.getDescription()).isEqualTo("수정된 설명");
            assertThat(response.getPlace().getName()).isEqualTo("수정된 장소");
            assertThat(response.getPlace().getAddress()).isEqualTo("기존 주소"); // 수정되지 않은 필드는 유지
            assertThat(response.getOwner().getId()).isEqualTo(ownerId);
        }

        @Test
        @DisplayName("모임장이 아닌 사용자가 수정 시도")
        void updateMeetingByNonOwner() {
            // given
            MeetingUpdateRequest request = new MeetingUpdateRequest();
            request.setName("수정된 모임");

            // when & then
            assertThatThrownBy(() -> meetingService.updateMeeting(1L, request, nonOwnerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모임장만 모임 정보를 수정할 수 있습니다.");
        }

        @Test
        @DisplayName("존재하지 않는 모임 수정 시도")
        void updateNonExistentMeeting() {
            // given
            when(meetingRepository.findById(999L)).thenReturn(java.util.Optional.empty());
            MeetingUpdateRequest request = new MeetingUpdateRequest();
            request.setName("수정된 모임");

            // when & then
            assertThatThrownBy(() -> meetingService.updateMeeting(999L, request, ownerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모임을 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("일부 필드만 수정")
        void updatePartialFields() {
            // given
            MeetingUpdateRequest request = new MeetingUpdateRequest();
            request.setName("수정된 모임");
            // description은 수정하지 않음

            // when
            MeetingResponse response = meetingService.updateMeeting(1L, request, ownerId);

            // then
            assertThat(response.getName()).isEqualTo("수정된 모임");
            assertThat(response.getDescription()).isEqualTo("기존 설명"); // 수정되지 않은 필드는 유지
            assertThat(response.getOwner().getId()).isEqualTo(ownerId);
        }
    }

    @Nested
    @DisplayName("모임 멤버 조회 테스트")
    class GetMeetingMembersTest {
        private Meeting testMeeting;
        private List<Member> members;

        @BeforeEach
        void setUp() {
            testMeeting = new Meeting();
            testMeeting.setId(1L);
            testMeeting.setName("테스트 모임");
            members = new ArrayList<>();

            // 멤버 3명 추가 (1명은 탈퇴)
            for (int i = 1; i <= 3; i++) {
                Auth auth = new Auth();
                auth.setId((long) i);
                auth.setNickname("테스트 유저 " + i);
                auth.setProfileImage("profile" + i + ".jpg");

                Member member = Member.createMember(testMeeting, auth, "닉네임 " + i);
                member.setId((long) i);
                if (i == 3) {
                    member.setWithdrawn(true); // 3번 멤버는 탈퇴 처리
                }
                members.add(member);
            }
            testMeeting.setMembers(members);

            // Mockito 설정
            lenient().when(meetingRepository.findById(1L)).thenReturn(java.util.Optional.of(testMeeting));
        }

        @Test
        @DisplayName("모임 멤버 목록 정상 조회")
        void getMeetingMembersSuccess() {
            // when
            List<MemberProfileResponse> responses = meetingService.getMeetingMembers(1L);

            // then
            assertThat(responses).hasSize(2); // 탈퇴한 멤버 제외
            assertThat(responses.get(0).getMemberId()).isEqualTo(1L);
            assertThat(responses.get(0).getNickname()).isEqualTo("닉네임 1");
            assertThat(responses.get(0).getProfileImageUrl()).isEqualTo("profile1.jpg");
        }

        @Test
        @DisplayName("존재하지 않는 모임의 멤버 조회")
        void getMeetingMembersWithNonExistentMeeting() {
            // given
            when(meetingRepository.findById(999L)).thenReturn(java.util.Optional.empty());

            // when & then
            assertThatThrownBy(() -> meetingService.getMeetingMembers(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모임을 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("모임 상세 조회 테스트")
    class GetMeetingDetailTest {
        private Meeting testMeeting;
        private Member ownerMember;

        @BeforeEach
        void setUp() {
            // 모임장 Auth 설정
            Auth ownerAuth = new Auth();
            ownerAuth.setId(1L);
            ownerAuth.setNickname("모임장");
            ownerAuth.setProfileImage("owner_profile.jpg");
            
            // 모임 설정
            testMeeting = new Meeting();
            testMeeting.setId(1L);
            testMeeting.setName("테스트 모임");
            testMeeting.setDescription("테스트 모임 설명");
            testMeeting.setOwnerId(1L);
            testMeeting.setStartTime(LocalDateTime.now().plusDays(1));
            testMeeting.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

            // 장소 설정
            Place place = new Place();
            place.setName("테스트 장소");
            place.setAddress("서울시 강남구");
            place.setLatitude(37.5);
            place.setLongitude(127.0);
            testMeeting.setPlace(place);

            // 모임장 멤버 설정
            ownerMember = Member.createMember(testMeeting, ownerAuth, "모임장");
            ownerMember.setId(1L);
            testMeeting.getMembers().add(ownerMember);

            // 일반 멤버 추가
            Auth memberAuth = new Auth();
            memberAuth.setId(2L);
            memberAuth.setNickname("일반 멤버");
            memberAuth.setProfileImage("member_profile.jpg");
            
            Member normalMember = Member.createMember(testMeeting, memberAuth, "일반 멤버");
            normalMember.setId(2L);
            testMeeting.getMembers().add(normalMember);

            // Mockito 설정
            lenient().when(meetingRepository.findById(1L)).thenReturn(java.util.Optional.of(testMeeting));
        }

        @Test
        @DisplayName("모임 상세 정보 정상 조회")
        void getMeetingDetailSuccess() {
            // when
            MeetingResponse response = meetingService.getMeetingDetail(1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("테스트 모임");
            assertThat(response.getDescription()).isEqualTo("테스트 모임 설명");
            
            // 장소 정보 확인
            assertThat(response.getPlace()).isNotNull();
            assertThat(response.getPlace().getName()).isEqualTo("테스트 장소");
            assertThat(response.getPlace().getAddress()).isEqualTo("서울시 강남구");
            
            // 모임장 정보 확인
            assertThat(response.getOwner()).isNotNull();
            assertThat(response.getOwner().getId()).isEqualTo(1L);
            assertThat(response.getOwner().getNickname()).isEqualTo("모임장");
            
            // 멤버 목록 확인
            assertThat(response.getMembers()).hasSize(2);
        }

        @Test
        @DisplayName("존재하지 않는 모임 조회")
        void getMeetingDetailNotFound() {
            // given
            when(meetingRepository.findById(999L)).thenReturn(java.util.Optional.empty());

            // when & then
            assertThatThrownBy(() -> meetingService.getMeetingDetail(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모임을 찾을 수 없습니다.");
        }
    }
} 