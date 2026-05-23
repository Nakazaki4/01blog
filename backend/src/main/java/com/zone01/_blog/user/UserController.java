package com.zone01._blog.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zone01._blog.user.dto.UserProfileResponse;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/users/{id}")
    public ResponseEntity<UserProfileResponse> user(
            @PathVariable Long id,
            @AuthenticationPrincipal String principal) {
        Long viewerId = (principal == null || "anonymousUser".equals(principal)) ? -1L : Long.parseLong(principal);
        return ResponseEntity.ok(userService.getProfile(id, viewerId));
    }

    @GetMapping("/api/users/{id}/posts?page={page_number}")
    public ResponseEntity userFeed(@PathVariable String id, @PathVariable String page_number) {
        return null;
    }

    @GetMapping("/api/users/{user_id}/subscribers?page={page_number}")
    public ResponseEntity subsribers(@PathVariable String user_id, @PathVariable String page_number) {
        return null;
    }

    @GetMapping("/api/users/{user_id}/subscriptions?page={page_number}")
    public ResponseEntity subscriptions(@PathVariable String user_id, @PathVariable String page_number) {
        return null;
    }

    @PostMapping("/api/users/{user_id}/subscribe")
    public ResponseEntity subscribe(@PathVariable String user_id) {
        return null;
    }

    @DeleteMapping("/api/users/{user_id}/subscribe")
    public ResponseEntity unsubscribe(@PathVariable String user_id) {
        return null;
    }

    @PatchMapping("/api/users/me")
    public ResponseEntity update(@PathVariable String user_id) {
        return null;
    }
}
