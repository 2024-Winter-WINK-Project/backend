package com.WinkProject.post.domain;

import com.WinkProject.meeting.domain.Meeting;
import com.WinkProject.member.domain.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "게시글")
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetingId")
    private Meeting meeting;
    
    private String title;
    private String content;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writerId")
    private Member writer;
    
    private LocalDateTime createdAt;
} 