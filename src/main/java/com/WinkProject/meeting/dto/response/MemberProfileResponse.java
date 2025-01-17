package com.WinkProject.meeting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileResponse {
    private Long memberId;
    private String nickname;
    private String profileImageUrl;
} 