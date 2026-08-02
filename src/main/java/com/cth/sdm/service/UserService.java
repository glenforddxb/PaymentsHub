package com.cth.sdm.service;

import com.cth.sdm.model.Role;
import com.cth.sdm.model.User;
import com.cth.sdm.repository.RoleRepository;
import com.cth.sdm.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void seedDefaultUsers() {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(null, "ROLE_ADMIN"));
            roleRepository.save(new Role(null, "ROLE_MAKER"));
            roleRepository.save(new Role(null, "ROLE_CHECKER"));
        }

        if (userRepository.count() == 0) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
            Role makerRole = roleRepository.findByName("ROLE_MAKER").orElseThrow();
            Role checkerRole = roleRepository.findByName("ROLE_CHECKER").orElseThrow();

            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("password"))
                    .email("admin@cth.com")
                    .isLdap(false)
                    .isLocked(false)
                    .isEnabled(true)
                    .roles(new HashSet<>(Set.of(adminRole, makerRole, checkerRole)))
                    .build();
            userRepository.save(admin);

            User maker = User.builder()
                    .username("maker")
                    .password(passwordEncoder.encode("password"))
                    .email("maker@cth.com")
                    .isLdap(false)
                    .isLocked(false)
                    .isEnabled(true)
                    .roles(new HashSet<>(Set.of(makerRole)))
                    .build();
            userRepository.save(maker);

            User checker = User.builder()
                    .username("checker")
                    .password(passwordEncoder.encode("password"))
                    .email("checker@cth.com")
                    .isLdap(false)
                    .isLocked(false)
                    .isEnabled(true)
                    .roles(new HashSet<>(Set.of(checkerRole)))
                    .build();
            userRepository.save(checker);
        }
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User createUser(User user, String roleName) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role role = roleRepository.findByName(roleName).orElseThrow();
        user.setRoles(new HashSet<>(Set.of(role)));
        return userRepository.save(user);
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
