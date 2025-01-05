package com.WinkProject.invitation.domain;

import com.WinkProject.meeting.domain.Meeting;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "초대")
public class Invitation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetingId")
    private Meeting meeting;
    
    @Column(unique = true)
    private String inviteCode;
    
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.expiresAt = this.createdAt.plusHours(24);
    }
} 