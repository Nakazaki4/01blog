package com.zone01._blog.settings;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.zone01._blog.settings.dto.ChangeEmailRequest;
import com.zone01._blog.settings.dto.ChangePasswordRequest;
import com.zone01._blog.user.User;
import com.zone01._blog.user.UserRepository;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;
    private final UserRepository users;

    public SettingsController(SettingsService settingsService, UserRepository users) {
        this.settingsService = settingsService;
        this.users = users;
    }

    @PatchMapping("/email")
    public ResponseEntity<Void> changeEmail(
            @AuthenticationPrincipal String principal,
            @Validated @RequestBody ChangeEmailRequest req) {
        settingsService.changeEmail(resolveUser(principal), req.email());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal String principal,
            @Validated @RequestBody ChangePasswordRequest req) {
        settingsService.changePassword(resolveUser(principal), req.currentPassword(), req.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/avatar", consumes = "multipart/form-data")
    public ResponseEntity<Void> changeAvatar(
            @AuthenticationPrincipal String principal,
            @RequestPart("avatar") MultipartFile avatar) {
        settingsService.changeAvatar(resolveUser(principal), avatar);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal String principal) {
        settingsService.deleteAccount(resolveUser(principal));
        return ResponseEntity.noContent().build();
    }

    private User resolveUser(String principal) {
        return users.findById(Long.parseLong(principal))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
