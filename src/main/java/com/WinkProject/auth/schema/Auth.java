package com.WinkProject.auth.schema;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import com.WinkProject.member.domain.Member;

@Entity
@Table(name = "인증")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Auth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String socialId;      // 소셜 로그인 ID

    @Column
    private String socialType;    // 소셜 로그인 타입 (예: KAKAO, NAVER 등)

    @Column
    private String nickname;      // 소셜 계정 닉네임

    @Column
    private String profileImage;  // 소셜 계정 프로필 이미지

    @OneToMany(mappedBy = "auth", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Member> members = new ArrayList<>();
}
