package com.cth.sdm.security;

import com.cth.sdm.service.CustomUserDetailsService;
import com.cth.sdm.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AuthenticationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Test
    public void testDefaultUserInitialization() {
        UserDetails adminDetails = userDetailsService.loadUserByUsername("admin");
        assertNotNull(adminDetails);
        assertTrue(adminDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    public void testSimulatedLdapAuthentication() {
        userDetailsService.setLdapEnabled(true);
        UserDetails ldapUser = userDetailsService.loadUserByUsername("ldapuser");
        assertNotNull(ldapUser);
        assertEquals("ldapuser", ldapUser.getUsername());
        userDetailsService.setLdapEnabled(false);
    }
}
