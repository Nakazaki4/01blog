package com.zone01._blog.auth;

import com.zone01._blog.auth.dto.LoginRequest;
import com.zone01._blog.auth.dto.LoginResponse;
import com.zone01._blog.auth.dto.SignupRequest;
import com.zone01._blog.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping()
    @RequestMapping("/signup")
    public ResponseEntity<Void> signupController(@RequestBody SignupRequest signupRequest) {
        authService.signup(signupRequest);
        return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create("/api/auth/login")).build();
    }

    @PostMapping()
    @RequestMapping("/login")
    public ResponseEntity<LoginResponse> loginController(@RequestBody LoginRequest loginRequest){
        User user = authService.login(loginRequest);
        String token = jwtService.issue(user);
        return ResponseEntity.ok(new LoginResponse(token, user.getId(), user.getUsername()));
    }
}
