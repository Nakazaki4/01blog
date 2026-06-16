package com.zone01._blog.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.zone01._blog.auth.dto.LoginRequest;
import com.zone01._blog.auth.dto.SignupRequest;
import com.zone01._blog.user.Role;
import com.zone01._blog.user.User;
import com.zone01._blog.user.UserRepository;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public User signup(SignupRequest req) {
        if (users.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        if (users.existsByUsername(req.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is taken");
        }
        User user = User.builder()
                .username(req.username())
                .email(req.email())
                .passwordHash(encoder.encode(req.password()))
                .avatarUrl(req.avatarUrl() != null ? req.avatarUrl() : "")
                .role(Role.USER)
                .banned(false)
                .build();
        return users.save(user);
    }

    public User login(LoginRequest req) {
        User user = users.findByUsernameOrEmail(req.username(), req.username()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username/Passord is incorrect"));
        if (user.isBanned()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is banned");
        }
        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username/Password is incorrect");
        }
        return user;
    }
}
