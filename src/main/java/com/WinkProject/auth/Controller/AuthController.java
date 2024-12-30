package com.WinkProject.auth.Controller;

import com.WinkProject.auth.dto.KakaoUserInfoResponse;
import com.WinkProject.auth.service.AuthService;
import com.WinkProject.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;



@Slf4j
@Controller
//@RequestMapping("/auth") TODO 마지막 testurl 없애기
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect_uri}")
    private String redirectUri;

    @GetMapping("/tt")
    public String test(){
        return "test";
    }

    @GetMapping("/auth/kakao/login")
    public String loginPage(){
        String location = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id="+clientId+"&redirect_uri="+redirectUri;
        return "redirect:" + location;
    }
    @GetMapping("/auth/callback") //TODO 나중에 /kakao/login 로 변경 후 프론트에서 인가 코드만 받아오기
    public ResponseEntity<?> callback(@RequestParam("code") String code, HttpServletResponse response)  {
        String accessToken = authService.getAccessToken(code);
        KakaoUserInfoResponse kakaoUserInfoResponse = authService.getUserInfo(accessToken);
        String token = jwtTokenProvider.createToken(kakaoUserInfoResponse.getId());

        //쿠키 생성,jwt 전달
        Cookie cookie = new Cookie("jwt",token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60*60); // 1시간 동안 유효
        response.addCookie(cookie);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
