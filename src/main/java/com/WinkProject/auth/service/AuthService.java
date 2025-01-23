package com.WinkProject.auth.service;

import com.WinkProject.auth.dto.KakaoTokenResponse;
import com.WinkProject.auth.dto.KakaoUserInfoResponse;
import com.WinkProject.auth.dto.UserInfoResponse;
import com.WinkProject.auth.repository.AuthRepository;
import com.WinkProject.auth.schema.Auth;
import io.netty.handler.codec.http.HttpHeaderValues;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect_uri}")
    private String redirectUri;

    private final AuthRepository authRepository;

    private static final String KAUTH_TOKEN_URL_HOST = "https://kauth.kakao.com";
    private static final String KAUTH_USER_URL_HOST = "https://kapi.kakao.com";

    public String getAccessToken(String code){
        KakaoTokenResponse kakaoTokenResponse = WebClient.create(KAUTH_TOKEN_URL_HOST).post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/oauth/token")
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", clientId)
                        .queryParam("redirect_uri",redirectUri)
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
    public void kakaoLogOut(String accessToken){
        WebClient.create(KAUTH_USER_URL_HOST).post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("v1/user/unlink")
                        .build(true))
                .header(HttpHeaders.AUTHORIZATION,"Bearer "+accessToken)
                .header(HttpHeaders.CONTENT_TYPE,HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .exchangeToMono(response -> Mono.empty())
                .block();
    }

    public UserInfoResponse saveAuth(Long userId,String nickName, String profileUrl){
        Auth existAuth = authRepository.findById(userId).orElse(null);
        if(existAuth == null){
            Auth auth = Auth.builder().socialId(userId).nickName(nickName).profileUrl(profileUrl).build();
            authRepository.save(auth);
            return UserInfoResponse.builder().loginState("REGISTER").memberId(userId).nickName(nickName).profileUrl(profileUrl).build();
        }
        else{
            return UserInfoResponse.builder().loginState("EXIST").memberId(userId).nickName(nickName).profileUrl(profileUrl).build();
        }


    }
    public boolean deleteAuth(Long userId){
        Optional<Auth> existAuth = authRepository.findById(userId);
        if (existAuth.isPresent() && existAuth.get().getMembers().isEmpty()){ //TODO 회원 탈퇴 거절 케이스 나누기
            authRepository.deleteById(userId);
            return true;
        }
        else{
            return false;
        }
    }

    public boolean logout(HttpServletRequest request, HttpServletResponse response){
        HttpSession session = request.getSession(false);

        if(session == null){
            return false;
        }
        String accessToken = (String)session.getAttribute("accessToken");
        kakaoLogOut(accessToken);

        session.invalidate();

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
