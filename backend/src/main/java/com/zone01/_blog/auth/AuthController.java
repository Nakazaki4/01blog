package com.zone01._blog.auth;

import com.zone01._blog.auth.dto.LoginRequest;
import com.zone01._blog.auth.dto.LoginResponse;
import com.zone01._blog.auth.dto.SignupRequest;
import com.zone01._blog.user.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> signupController(@Valid @RequestBody SignupRequest signupRequest) {
        User user = authService.signup(signupRequest);
        String token = jwtService.issue(user);
        return ResponseEntity.ok(new LoginResponse(token, user.getId(),
                user.getUsername(), user.getRole()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginController(@Valid @RequestBody LoginRequest loginRequest) {
        User user = authService.login(loginRequest);
        String token = jwtService.issue(user);
        return ResponseEntity.ok(new LoginResponse(token, user.getId(),
                user.getUsername(), user.getRole()));
    }
}
