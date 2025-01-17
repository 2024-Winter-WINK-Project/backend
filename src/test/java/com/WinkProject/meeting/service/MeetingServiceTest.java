package com.WinkProject.meeting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.WinkProject.meeting.domain.Meeting;
import com.WinkProject.meeting.domain.Settlement;
import com.WinkProject.meeting.dto.request.MeetingCreateRequest;
import com.WinkProject.meeting.dto.response.MeetingResponse;
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
    private MemberRepository memberRepository;

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private AuthRepository authRepository;

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

        MeetingCreateRequest.PlaceRequest placeRequest = new MeetingCreateRequest.PlaceRequest();
        placeRequest.setName("테스트 장소");
        placeRequest.setAddress("서울시 강남구");
        placeRequest.setLatitude(37.5);
        placeRequest.setLongitude(127.0);
        request.setPlace(placeRequest);
    }

    @Nested
    @DisplayName("모임 생성 테스트")
    class CreateMeetingTest {
        
        @Test
        @DisplayName("정산 정보 없이 모임 생성")
        void createMeetingWithoutSettlement() {
            // given
            when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> {
                Meeting meeting = invocation.getArgument(0);
                meeting.setId(1L);
                
                // 모임장 Member 설정 시 Auth 연결
                Member owner = meeting.getMembers().get(0);
                owner.setId(1L);
                owner.setAuth(testAuth);
                owner.setNickname("모임장");
                owner.setProfileImage("default.png");
                
                return meeting;
            });

            // when
            MeetingResponse response = meetingService.createMeeting(request, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("테스트 모임");
            assertThat(response.getOwner().getNickname()).isEqualTo("모임장");
            assertThat(response.getSettlement()).isNull();
            verify(meetingRepository).save(any(Meeting.class));
        }

        @Test
        @DisplayName("정산 정보 포함하여 모임 생성")
        void createMeetingWithSettlement() {
            // given
            MeetingCreateRequest.SettlementRequest settlementRequest = new MeetingCreateRequest.SettlementRequest();
            settlementRequest.setKakaoPayString("카카오페이");
            settlementRequest.setTossPayString("토스");
            settlementRequest.setAccountNumber("1234-5678");
            request.setSettlement(settlementRequest);

            when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> {
                Meeting meeting = invocation.getArgument(0);
                meeting.setId(1L);
                
                // 모임장 Member 설정 시 Auth 연결
                Member owner = meeting.getMembers().get(0);
                owner.setId(1L);
                owner.setAuth(testAuth);
                owner.setNickname("모임장");
                owner.setProfileImage("default.png");
                
                return meeting;
            });

            // when
            MeetingResponse response = meetingService.createMeeting(request, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getSettlement()).isNotNull();
            assertThat(response.getSettlement().getKakaoPayString()).isEqualTo("카카오페이");
            assertThat(response.getSettlement().getTossPayString()).isEqualTo("토스");
            assertThat(response.getSettlement().getAccountNumber()).isEqualTo("1234-5678");
            verify(meetingRepository).save(any(Meeting.class));
            verify(settlementRepository).save(any(Settlement.class));
        }

        @Test
        @DisplayName("모임 생성 시 모임장 정보 확인")
        void checkOwnerInfoAfterMeetingCreation() {
            // given
            when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> {
                Meeting meeting = invocation.getArgument(0);
                meeting.setId(1L);
                
                // 모임장 Member 설정 시 Auth 연결
                Member owner = meeting.getMembers().get(0);
                owner.setId(1L);
                owner.setAuth(testAuth);
                owner.setNickname("모임장");
                owner.setProfileImage("default.png");
                
                return meeting;
            });

            // when
            MeetingResponse response = meetingService.createMeeting(request, 1L);

            // then
            assertThat(response.getOwner()).isNotNull();
            assertThat(response.getOwner().getId()).isEqualTo(1L);
            assertThat(response.getOwner().getNickname()).isEqualTo("모임장");
            assertThat(response.getOwner().getProfileImage()).isEqualTo("default.png");
        }
    }
} 