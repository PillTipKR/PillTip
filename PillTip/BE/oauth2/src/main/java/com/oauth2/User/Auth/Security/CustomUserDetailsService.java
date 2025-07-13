// author : mireutale
// description : 사용자 세부 정보 서비스
package com.oauth2.User.Auth.Security;

import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.Auth.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import com.oauth2.User.Auth.Dto.AuthMessageConstants;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    /*
     * 스프링 시큐리티가 로그인 요청 시 호출하는 메서드
     * 사용자 ID를 기반으로 사용자 정보를 조회하고, 인증에 필요한 UserDetails 객체를 반환함
     */
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        // 사용자 ID를 기반으로 사용자 정보를 조회
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new UsernameNotFoundException(AuthMessageConstants.USER_INFO_NOT_FOUND_RETRY_LOGIN + " with id: " + userId));

        // 사용자 정보가 없으면 예외 발생
        if (user == null) {
            throw new UsernameNotFoundException(AuthMessageConstants.USER_INFO_NOT_FOUND_RETRY_LOGIN + " with id: " + userId);
        }

        // 사용자 정보를 기반으로 UserDetails 객체 생성
        return new org.springframework.security.core.userdetails.User(
                String.valueOf(user.getId()), // 사용자 ID
                user.getPasswordHash() != null ? user.getPasswordHash() : "", // 사용자 비밀번호
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // 사용자 권한
        );
    }
}
