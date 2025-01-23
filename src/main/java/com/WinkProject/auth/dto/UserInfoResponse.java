package com.WinkProject.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponse {
    public String loginState;
    public Long memberId;
    public String nickName;
    public String profileUrl;

}
