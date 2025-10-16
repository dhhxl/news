package com.news.config;

import com.news.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security配置类
 * 配置JWT认证、授权规则和安全过滤器链
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * 配置安全过滤器链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 启用CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            
            // 禁用CSRF（使用JWT时不需要）
            .csrf(AbstractHttpConfigurer::disable)
            
            // 配置授权规则
            .authorizeHttpRequests(auth -> auth
                // 公开端点 - 无需认证
                .requestMatchers(
                    "/",
                    "/auth/login",
                    "/auth/register",
                    "/auth/logout",
                    "/actuator/health",
                    "/actuator/info",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()
                
                // 公开的GET请求 - 无需认证
                .requestMatchers(org.springframework.http.HttpMethod.GET, 
                    "/categories",
                    "/categories/**",
                    "/news",
                    "/news/**",
                    "/summaries/**",
                    "/comments/**",
                    "/likes/**"
                ).permitAll()
                
                // 管理员端点 - 需要ADMIN角色
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // 其他所有端点 - 需要认证
                .anyRequest().authenticated()
            )
            
            // 无状态会话管理（JWT）
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 添加JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, 
                           UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码编码器 - BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证提供者
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

