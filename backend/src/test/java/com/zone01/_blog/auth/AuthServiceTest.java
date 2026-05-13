package com.zone01._blog.auth;

import com.zone01._blog.auth.dto.LoginRequest;
import com.zone01._blog.auth.dto.SignupRequest;
import com.zone01._blog.user.Role;
import com.zone01._blog.user.User;
import com.zone01._blog.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository users;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private AuthService authService;

    private SignupRequest signupReq;
    private LoginRequest loginReq;

    @BeforeEach
    void setUp() {
        signupReq = new SignupRequest("alice", "alice@example.com", "secret", "hi", null);
        loginReq = new LoginRequest("alice", "secret");
    }

    @Test
    void signup_persistsUserWithHashedPassword() {
        when(users.existsByEmail(signupReq.email())).thenReturn(false);
        when(users.existsByUsername(signupReq.username())).thenReturn(false);
        when(encoder.encode("secret")).thenReturn("HASHED");

        authService.signup(signupReq);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(users).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getUsername()).isEqualTo("alice");
        assertThat(saved.getEmail()).isEqualTo("alice@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("HASHED");
        assertThat(saved.getBio()).isEqualTo("hi");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        assertThat(saved.isBanned()).isFalse();
    }

    @Test
    void signup_throwsConflict_whenEmailExists() {
        when(users.existsByEmail(signupReq.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(signupReq))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.CONFLICT);

        verify(users, never()).save(any());
    }

    @Test
    void signup_throwsConflict_whenUsernameExists() {
        when(users.existsByEmail(signupReq.email())).thenReturn(false);
        when(users.existsByUsername(signupReq.username())).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(signupReq))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.CONFLICT);

        verify(users, never()).save(any());
    }

    @Test
    void login_returnsUser_onValidCredentials() {
        User user = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@example.com")
                .passwordHash("HASHED")
                .build();
        when(users.findByUsername("alice")).thenReturn(Optional.of(user));
        when(encoder.matches("secret", "HASHED")).thenReturn(true);

        User result = authService.login(loginReq);

        assertThat(result).isSameAs(user);
    }

    @Test
    void login_throwsUnauthorized_whenUserNotFound() {
        when(users.findByUsername("alice")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginReq))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_throwsUnauthorized_whenPasswordWrong() {
        User user = User.builder()
                .username("alice")
                .passwordHash("HASHED")
                .build();
        when(users.findByUsername("alice")).thenReturn(Optional.of(user));
        when(encoder.matches(eq("secret"), eq("HASHED"))).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginReq))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_throwsForbidden_whenUserIsBanned() {
        User user = User.builder()
                .username("alice")
                .passwordHash("HASHED")
                .banned(true)
                .build();
        when(users.findByUsername("alice")).thenReturn(Optional.of(user));
        when(encoder.matches("secret", "HASHED")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(loginReq))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);
    }
}
