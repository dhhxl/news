package com.news.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 拦截每个请求，验证JWT Token并设置认证信息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) 
            throws ServletException, IOException {
        
        try {
            // 从请求中提取JWT Token
            String jwt = extractJwtFromRequest(request);

            // 验证Token并设置认证信息
            if (StringUtils.hasText(jwt) 
                    && jwtTokenProvider.isValidTokenFormat(jwt)) {
                
                String username = jwtTokenProvider.extractUsername(jwt);

                if (username != null 
                        && SecurityContextHolder.getContext()
                            .getAuthentication() == null) {
                    
                    // 加载用户详情
                    UserDetails userDetails = 
                        userDetailsService.loadUserByUsername(username);

                    // 验证Token
                    if (jwtTokenProvider.validateToken(jwt, userDetails)) {
                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                            );
                        
                        authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                        );

                        // 设置到SecurityContext
                        SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                        
                        log.debug("JWT authentication successful for user: {}", 
                                username);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", 
                     ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取JWT Token
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(bearerToken) 
                && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }
}

