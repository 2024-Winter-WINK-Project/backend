package com.WinkProject.member.domain;

import com.WinkProject.meeting.domain.Meeting;
import com.WinkProject.auth.schema.Auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "멤버")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    @ManyToOne
    @JoinColumn(name = "auth_id")
    private Auth auth;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private boolean isWithdrawn = false;

    public String getProfileImage() {
        return auth != null ? auth.getProfileImage() : null;
    }

    public static Member createMember(Meeting meeting, Auth auth, String nickname) {
        Member member = new Member();
        member.setMeeting(meeting);
        member.setAuth(auth);
        member.setNickname(nickname);
        return member;
    }
} 