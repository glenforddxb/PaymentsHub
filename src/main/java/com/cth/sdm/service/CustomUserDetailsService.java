package com.cth.sdm.service;

import com.cth.sdm.model.User;
import com.cth.sdm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private boolean ldapEnabled = false;

    public boolean isLdapEnabled() {
        return ldapEnabled;
    }

    public void setLdapEnabled(boolean ldapEnabled) {
        this.ldapEnabled = ldapEnabled;
        log.info("Dual Authentication mode configured. LDAP Enabled state set to: {}", ldapEnabled);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (ldapEnabled) {
            log.info("Authenticating via Simulated Active Directory LDAP for user: {}", username);
            if ("ldapuser".equalsIgnoreCase(username)) {
                return org.springframework.security.core.userdetails.User.builder()
                        .username("ldapuser")
                        .password("{noop}ldappassword")
                        .roles("MAKER")
                        .build();
            } else if ("ldapadmin".equalsIgnoreCase(username)) {
                return org.springframework.security.core.userdetails.User.builder()
                        .username("ldapadmin")
                        .password("{noop}ldappassword")
                        .roles("ADMIN", "MAKER", "CHECKER")
                        .build();
            }
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (!user.isEnabled()) {
            throw new RuntimeException("User account is disabled");
        }
        if (user.isLocked()) {
            throw new RuntimeException("User account is locked");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toList())
        );
    }
}
