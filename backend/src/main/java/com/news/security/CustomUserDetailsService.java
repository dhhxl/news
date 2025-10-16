package com.news.security;

import com.news.model.entity.User;
import com.news.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

/**
 * 自定义用户详情服务
 * 实现Spring Security的UserDetailsService接口
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) 
            throws UsernameNotFoundException {
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));

        return new CustomUserDetails(user);
    }

    /**
     * 自定义UserDetails实现
     */
    public static class CustomUserDetails implements UserDetails {
        
        private final User user;

        public CustomUserDetails(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // 角色需要加ROLE_前缀
            String role = "ROLE_" + user.getRole();
            return Collections.singletonList(
                new SimpleGrantedAuthority(role)
            );
        }

        @Override
        public String getPassword() {
            return user.getPasswordHash();
        }

        @Override
        public String getUsername() {
            return user.getUsername();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        public Long getUserId() {
            return user.getId();
        }

        public String getRole() {
            return user.getRole();
        }

        public User getUser() {
            return user;
        }
    }
}

