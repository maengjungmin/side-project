package org.side.mjm.domain.authority.login;

import jakarta.transaction.Transactional;
import org.side.mjm.common.exception.code.CommonErrorCode;
import org.side.mjm.common.exception.custom.UnauthorizedException;
import org.side.mjm.domain.user.UserRepository;
import org.side.mjm.entity.m_user;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 인증정보 서비스<br />
 * AuthController 에서 authenticationManagerBuilder 에 의해 호출되며 인증정보를 포함한<br />
 * Security UserDetail 정보를 리턴한다.<br />
 *
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return userRepository.findById(userId).map(this::createUser)
                .orElseThrow(() -> new UsernameNotFoundException(userId + " -> not found."));
    }

    /**
     * Security User 정보를 생성한다.
     * BadCredentialsException 은 JwtAuthenticationEntryPoint 로 전달되기 때문에 불필요 로직을 타게 됨.<br />
     **/
    private User createUser(m_user user) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        return new User(
                user.getUserId(),
                user.getPassword(),
                grantedAuthorities
        );
    }
}