package com.WinkProject.auth.service;

import com.WinkProject.auth.dto.KakaoTokenResponse;
import com.WinkProject.auth.dto.KakaoUserInfoResponse;
import com.WinkProject.auth.repository.AuthRepository;
import com.WinkProject.auth.repository.MemberRepository;
import com.WinkProject.auth.schema.Auth;
import io.netty.handler.codec.http.HttpHeaderValues;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    private final AuthRepository authRepository;
    private final MemberRepository memberRepository;

    private static final String KAUTH_TOKEN_URL_HOST = "https://kauth.kakao.com";
    private static final String KAUTH_USER_URL_HOST = "https://kapi.kakao.com";

    public String getAccessToken(String code){
        KakaoTokenResponse kakaoTokenResponse = WebClient.create(KAUTH_TOKEN_URL_HOST).post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/oauth/token")
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", clientId)
                        .queryParam("code", code)
                        .build(true))
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                .bodyToMono(KakaoTokenResponse.class)
                .block();

        return kakaoTokenResponse.getAccessToken();
    }

    public KakaoUserInfoResponse getUserInfo(String accessToken){
        KakaoUserInfoResponse kakaoUserInfoResponse = WebClient.create(KAUTH_USER_URL_HOST).get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/v2/user/me")
                        .build(true))
                .header(HttpHeaders.AUTHORIZATION,"Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE,HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                .bodyToMono(KakaoUserInfoResponse.class)
                .block();


        return kakaoUserInfoResponse;


    }

    public void saveAuth(Long userId, String profileUrl){
        //TODO 로그인 할때마다 저장 하지 말고 데이터 있는지 먼저 검사 하기
        Auth auth = Auth.builder().socialId(userId).profileUrl(profileUrl).build();
        authRepository.save(auth);

    }
    public boolean deleteAuth(Long userId){
        Optional<Auth> Existauth = authRepository.findById(userId);
        if (Existauth.isPresent() && !memberRepository.existsBySocialId(userId)){ //TODO 회원 탈퇴 거절 케이스 나누기
            authRepository.deleteById(userId);
            return true;
        }
        else{
            return false;
        }
    }

    public boolean logout(HttpServletResponse response){
        Cookie cookie = new Cookie("jwt",null);
        cookie.setHttpOnly(true); // 생성 시와 동일하게 설정
        cookie.setSecure(true); // 생성 시와 동일하게 설정
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        log.info("쿠키 삭제 완료");
        return true;

    }

}
