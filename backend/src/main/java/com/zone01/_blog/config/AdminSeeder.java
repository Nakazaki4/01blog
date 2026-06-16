package com.zone01._blog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.zone01._blog.user.Role;
import com.zone01._blog.user.User;
import com.zone01._blog.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@Order(1)
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        if (userRepo.existsByEmail(adminEmail) || userRepo.existsByUsername(adminUsername)) return;

        User user = new User();
        user.setEmail(adminEmail);
        user.setUsername(adminUsername);
        user.setPasswordHash(encoder.encode(adminPassword));
        user.setRole(Role.ADMIN);
        userRepo.save(user);
    }
}
