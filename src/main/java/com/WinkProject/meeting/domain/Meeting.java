package com.WinkProject.meeting.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.WinkProject.budget.domain.Budget;
import com.WinkProject.invitation.domain.Invitation;
import com.WinkProject.member.domain.Member;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
// import jakarta.persistence.*; // TODO 문제 생길 시 import 교체
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "모임")
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerId;  // 모임장 ID

    private String name;
    private String description;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "placeId")
    private Place place;

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL)
    private List<Member> members = new ArrayList<>();

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL)
    private List<Invitation> invitations = new ArrayList<>();

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private Budget budget;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }


    // 모임장 관련 편의 메서드
    public boolean isOwner(Long userId) {
        return this.ownerId.equals(userId);
    }

    public void changeOwner(Long newOwnerId) {
        if (!members.stream().anyMatch(member -> member.getId().equals(newOwnerId))) {
            throw new IllegalArgumentException("모임의 멤버만 모임장이 될 수 있습니다.");
        }
        this.ownerId = newOwnerId;
    }
} 