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
                path.equals("/auth/kakao/login")|| // 인증 관련 경로
                path.equals("/auth/callback")||
                path.equals("/static/favicon.ico");
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
            Long userId = claims.get("userId", Long.class);


            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId,null,null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info(String.valueOf(authentication.getPrincipal().getClass()));
        }catch (Exception e){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request,response);
    }
}

