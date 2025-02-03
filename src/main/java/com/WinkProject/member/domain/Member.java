package com.WinkProject.member.domain;

import com.WinkProject.meeting.domain.Meeting;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetingId")
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authId")
    private Auth auth;

    private String nickname;
    private String profileImage;
    private boolean isWithdrawn;

    public static Member createMember(Meeting meeting, Auth auth, String nickname) {
        Member member = new Member();
        member.setMeeting(meeting);
        member.setAuth(auth);
        member.setNickname(nickname);
        member.setProfileImage(auth.getProfileImage());
        member.setWithdrawn(false);
        return member;
    }
} 