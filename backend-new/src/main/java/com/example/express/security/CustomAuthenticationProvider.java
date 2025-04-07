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
        boolean isStaff = false;
        String salt = "";

        // Check if the user is a staff member to apply salting logic
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            if ("ROLE_STAFF".equals(authority.getAuthority())) {
                isStaff = true;
                break;
            }
        }

        boolean passwordMatches;

        if (isStaff) {
            log.info("User {} identified as STAFF. Proceeding with staff logic.", username);
            // For staff, retrieve phone number to get the salt
            Staff staff = null;
            try {
                log.info("Fetching Staff entity for {} to get salt.", username);
                staff = staffService.getByUsername(username); // Assuming getByUsername is efficient
                if (staff != null) {
                    log.info("Staff entity found for {}. Phone: {}", username, staff.getPhone());
                } else {
                     log.warn("Staff entity NOT found via staffService for {}, although UserDetails has ROLE_STAFF.", username);
                }
            } catch (Exception e) {
                 log.error("Error fetching Staff entity for {}: {}", username, e.getMessage(), e);
                 // Decide how to handle - fail auth or try without salt? For now, try without.
                 staff = null; // Ensure staff is null if error occurred
            }

            if (staff != null && staff.getPhone() != null && !staff.getPhone().isEmpty()) {
                String phone = staff.getPhone();
                salt = phone.substring(Math.max(0, phone.length() - 4));
                log.info("Comparing salted password for {}. Salt: '{}'", username, salt);
                // Compare submitted+salt with stored hash
                passwordMatches = passwordEncoder.matches(submittedPassword + salt, storedPassword);
                log.info("Salted comparison result for {}: {}", username, passwordMatches);
            } else {
                 // Staff found but no phone number, or staff not found by service, or error fetching staff
                 // Fallback to non-salted comparison
                 log.warn("Staff {} - phone missing, staff not found by service, or error fetching. Attempting non-salted match.", username);
                 passwordMatches = passwordEncoder.matches(submittedPassword, storedPassword);
                 log.info("Non-salted fallback comparison result for {}: {}", username, passwordMatches);
            }

            // Compatibility check for legacy unsalted staff passwords (if primary salted/fallback match failed)
            // This handles the case where the user logs in with the *old* unsalted password '123456'
            // after the hash in DB was manually reverted.
            if (!passwordMatches) {
                log.info("Primary salted/fallback match failed for staff {}. Trying unsalted match for legacy compatibility.", username);
                boolean legacyMatch = passwordEncoder.matches(submittedPassword, storedPassword);
                log.info("Legacy unsalted comparison result for {}: {}", username, legacyMatch);
                if (legacyMatch) {
                    log.warn("Staff {} logged in with legacy unsalted password. Consider prompting for password update.", username);
                    passwordMatches = true; // Allow login with the old unsalted password
                }
            }

        } else {
            // For non-staff users, assume no salt is used
            log.info("User {} identified as non-staff. Proceeding with direct password comparison.", username);
            passwordMatches = passwordEncoder.matches(submittedPassword, storedPassword);
            log.info("Direct comparison result for {}: {}", username, passwordMatches);
        }


        if (!passwordMatches) {
            log.warn("Authentication failed: Password does not match for username - {}. isStaff={}", username, isStaff);
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
