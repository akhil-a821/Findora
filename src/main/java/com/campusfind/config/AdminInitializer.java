package com.campusfind.config;

import com.campusfind.entity.User;
import com.campusfind.entity.enums.Role;
import com.campusfind.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail("admin@campus.edu")) {
            User admin = new User("System Administrator", "admin@campus.edu", passwordEncoder.encode("admin123"), Role.ROLE_ADMIN);
            userRepository.save(admin);
        }
    }
}
