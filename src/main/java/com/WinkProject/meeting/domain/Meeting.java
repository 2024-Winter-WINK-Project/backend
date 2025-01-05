package com.WinkProject.meeting.domain;

import com.WinkProject.invitation.domain.Invitation;
import com.WinkProject.member.domain.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "모임")
public class Meeting {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
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
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
} 