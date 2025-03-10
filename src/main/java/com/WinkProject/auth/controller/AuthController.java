package com.WinkProject.auth.controller;

import com.WinkProject.auth.dto.KakaoUserInfoResponse;
import com.WinkProject.auth.dto.UserInfoResponse;
import com.WinkProject.auth.service.AuthService;
import com.WinkProject.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Slf4j
@Controller

@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect_uri}")
    private String redirectUri;

    @GetMapping("/auth/kakao/login")
    @ResponseBody
    public ResponseEntity<UserInfoResponse> callback(@RequestParam("code") String code, HttpServletRequest request, HttpServletResponse response)  {
        String accessToken = authService.getAccessToken(code);

        HttpSession session = request.getSession();
        session.setAttribute("accessToken",accessToken);

        KakaoUserInfoResponse kakaoUserInfoResponse = authService.getUserInfo(accessToken);

        // DB에 auth 정보 저장 만약 이미 저장되어 있으면 continue
        Long userID = kakaoUserInfoResponse.getId();
        String profileUrl = kakaoUserInfoResponse.getKakaoAccount().getProfile().getProfileImageUrl();
        String nickName = kakaoUserInfoResponse.getKakaoAccount().getProfile().getNickName();

        //유저 정보 응답 생성
        UserInfoResponse userInfoResponse = authService.saveAuth(userID,nickName,profileUrl);

        //쿠키 생성,jwt 전달
        String token = jwtTokenProvider.createToken(userInfoResponse.getMemberId());
        Cookie cookie = new Cookie("jwt",token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60*30); // 30분 동안 유효
        response.addCookie(cookie);
        return ResponseEntity.ok(userInfoResponse);
    }

    @DeleteMapping("/auth/withdraw")
    @ResponseBody
    public ResponseEntity<?> withdraw(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long)authentication.getPrincipal();
        boolean withDrawSuccess = authService.deleteAuth(userId);

        if (withDrawSuccess){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    }

    @GetMapping("/auth/logout")
    @ResponseBody
    public ResponseEntity<?> logout(HttpServletRequest request,HttpServletResponse response){
        boolean logOutSuccess = authService.logout(request ,response);
        if (logOutSuccess){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    }

}
