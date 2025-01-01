package com.WinkProject.auth.schema;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Member {
    @Id
    private Long memberId;

    private Long socialId;
    private String nickname;
    private String defaultProfileImage;
    private boolean isWithdrawn;
}
