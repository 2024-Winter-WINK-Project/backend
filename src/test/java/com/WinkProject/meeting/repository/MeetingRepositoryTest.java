package com.winkproject.meeting.repository;

import com.winkproject.common.fixture.MeetingTestFixture;
import com.winkproject.meeting.domain.Meeting;
import com.winkproject.meeting.dto.response.MeetingResponse;
import com.winkproject.member.domain.Auth;
import com.winkproject.member.domain.Member;
import com.winkproject.member.domain.MemberRole;
import com.winkproject.member.repository.AuthRepository;
import com.winkproject.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MeetingRepositoryTest {

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthRepository authRepository;

    private Meeting testMeeting;
    private Member testMember;
    private Auth testAuth;

    @BeforeEach
    void setUp() {
        // Auth 생성 및 저장
        testAuth = new Auth();
        testAuth.setSocialId("test123");
        testAuth.setSocialType("KAKAO");
        testAuth.setNickname("테스트 유저");
        testAuth.setProfileImage("test.jpg");
        testAuth = authRepository.save(testAuth);

        // Meeting 생성 및 저장
        testMeeting = MeetingTestFixture.createMeeting("테스트 모임");
        testMeeting = meetingRepository.save(testMeeting);

        // Member 생성 및 저장
        testMember = new Member();
        testMember.setMeeting(testMeeting);
        testMember.setAuth(testAuth);
        testMember.setRole(MemberRole.OWNER);
        testMember.setNickname(testAuth.getNickname());
        testMember.setDefaultProfileImage(testAuth.getProfileImage());
        testMember = memberRepository.save(testMember);

        // Meeting에 Member 추가
        testMeeting.getMembers().add(testMember);
        meetingRepository.save(testMeeting);
    }

    @Test
    @DisplayName("모임 저장 - 성공")
    void saveMeeting_Success() {
        // given
        Meeting meeting = MeetingTestFixture.createMeeting("새로운 모임");

        // when
        Meeting savedMeeting = meetingRepository.save(meeting);

        // then
        assertThat(savedMeeting.getId()).isNotNull();
        assertThat(savedMeeting.getName()).isEqualTo("새로운 모임");
        assertThat(savedMeeting.getPlace()).isNotNull();
        assertThat(savedMeeting.getPlace().getName()).isEqualTo("테스트 장소");
    }

    @Test
    @DisplayName("모임 조회 - ID로 조회 성공")
    void findById_Success() {
        // when
        Meeting foundMeeting = meetingRepository.findById(testMeeting.getId())
                .orElseThrow();

        // then
        assertThat(foundMeeting).isNotNull();
        assertThat(foundMeeting.getName()).isEqualTo("테스트 모임");
        assertThat(foundMeeting.getMembers()).hasSize(1);
    }

    @Test
    @DisplayName("최근 모임 조회 - 성공")
    void findLatestMeetings_Success() {
        // given
        int limit = 5;

        // when
        List<MeetingResponse> meetings = meetingRepository.findLatestMeetingDTOs(testAuth.getId(), PageRequest.of(0, limit));

        // then
        assertThat(meetings).hasSize(1);
        assertThat(meetings.get(0).getName()).isEqualTo("테스트 모임");
    }

    @Test
    @DisplayName("모임 삭제 - 성공")
    void deleteMeeting_Success() {
        // when
        meetingRepository.delete(testMeeting);
        boolean exists = meetingRepository.existsById(testMeeting.getId());

        // then
        assertThat(exists).isFalse();
    }
} 