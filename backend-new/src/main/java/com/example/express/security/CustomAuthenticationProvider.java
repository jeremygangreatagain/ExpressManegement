package com.example.express.security;

import com.example.express.entity.Staff;
import com.example.express.service.StaffService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component // Register as a Spring bean
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final StaffService staffService;

    @Autowired
    public CustomAuthenticationProvider(@Lazy UserDetailsService userDetailsService,
                                        @Lazy PasswordEncoder passwordEncoder,
                                        @Lazy StaffService staffService) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.staffService = staffService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String submittedPassword = authentication.getCredentials().toString();
        log.info("Attempting authentication for username: {}", username);

        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(username);
            log.info("UserDetails loaded for {}. Authorities: {}", username, userDetails.getAuthorities());
        } catch (UsernameNotFoundException e) {
            log.warn("Authentication failed: Username not found - {}", username);
            throw new BadCredentialsException("用户名或密码错误");
        } catch (Exception e) {
            log.error("Error loading UserDetails for {}: {}", username, e.getMessage(), e);
            throw new BadCredentialsException("获取用户信息时出错");
        }


        String storedPassword = userDetails.getPassword();

        // 始终使用不加盐的方式比较密码
        log.info("Performing direct password comparison for user: {}", username);
        // Log values before matching
        log.debug("[AUTH] Comparing rawPassword: '{}' with encodedPassword: '{}'", submittedPassword, storedPassword);
        boolean passwordMatches = passwordEncoder.matches(submittedPassword, storedPassword);
        log.info("Direct comparison result for {}: {}", username, passwordMatches);

        if (!passwordMatches) {
            log.warn("Authentication failed: Password does not match for username - {}", username);
            throw new BadCredentialsException("用户名或密码错误");
        }

        log.info("Authentication successful for username: {}", username);
        // Return the fully authenticated object, principal should be UserDetails
        return new UsernamePasswordAuthenticationToken(userDetails, submittedPassword, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        // Indicate that this provider supports UsernamePasswordAuthenticationToken
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
