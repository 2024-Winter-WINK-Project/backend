package com.WinkProject.member.domain;

import com.WinkProject.meeting.domain.Meeting;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "회원")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetingId")
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "socialId")
    private Auth auth;

    private String nickname;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    private String defaultProfileImage;
    private boolean isWithdrawn;

    public static Member createOwner(Meeting meeting, Long userId) {
        Member member = new Member();
        member.setMeeting(meeting);
        member.setRole(MemberRole.OWNER);
        member.setNickname("모임장");
        member.setDefaultProfileImage("default.png");
        member.setWithdrawn(false);
        // TODO: Auth 정보는 추후 구현
        return member;
    }
} 