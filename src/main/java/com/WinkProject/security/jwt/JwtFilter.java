package com.WinkProject.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private boolean isPermitAllPath(String path) {
        return path.equals("/") || // 첫 화면 test용도
                path.startsWith("/auth/"); // 인증 관련 경로
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        if (isPermitAllPath(requestURI)) {
            log.info("PermitAllPath: {}", requestURI);
            filterChain.doFilter(request, response);
            return; // 필터 중단
        }
        String token = null;
        if(request.getCookies() != null){
            for (Cookie cookie : request.getCookies()){
                if("jwt".equals(cookie.getName())){
                    log.info("jwt존재");
                    token = cookie.getValue();
                }
            }
        }
        if(token == null){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        try{
            Claims claims = jwtTokenProvider.validateToken(token);
            String userId = claims.get("userId", Long.class).toString();
            log.info(claims.toString());
            log.info("토큰 디코딩 성공");

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId,null,null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }catch (Exception e){
            log.info("실패");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request,response);
    }
}

