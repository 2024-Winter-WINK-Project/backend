package com.WinkProject.auth.schema;

import jakarta.persistence.*;

@Entity
public class Member {
    @Id
    private Long memberId;

    private Long socialId;
    private String nickname;
    private String defaultProfileImage;
    private boolean isWithdrawn;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "socailId") // FK로 사용될 컬럼
    private Auth auth;
}
