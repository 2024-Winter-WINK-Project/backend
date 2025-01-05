package com.WinkProject.member.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "인증")
public class Auth {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String socialId;      // 소셜 로그인 ID
    private String socialType;    // 소셜 로그인 타입 (예: KAKAO, NAVER 등)
    private String nickname;      // 소셜 계정 닉네임
    private String profileImage;  // 소셜 계정 프로필 이미지
    
    @OneToMany(mappedBy = "auth")
    private List<Member> members = new ArrayList<>();
} 