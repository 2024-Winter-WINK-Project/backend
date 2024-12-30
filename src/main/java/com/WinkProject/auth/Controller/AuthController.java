package com.WinkProject.auth.Controller;

import com.WinkProject.auth.dto.KakaoUserInfoResponse;
import com.WinkProject.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect_uri}")
    private String redirectUri;

    @GetMapping("/kakao/login")
    public String loginPage(){
        String location = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id="+clientId+"&redirect_uri="+redirectUri;
        return "redirect:" + location;
    }
    @GetMapping("/callback") // 나중에 /kakao/login 로 변경 후 프론트에서 인가 코드만 받아오기
    public ResponseEntity<?> callback(@RequestParam("code") String code)  {
        String accessToken = authService.getAccessToken(code);
        KakaoUserInfoResponse kakaoUserInfoResponse = authService.getUserInfo(accessToken);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
