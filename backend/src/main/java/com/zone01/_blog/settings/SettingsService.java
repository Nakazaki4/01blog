package com.zone01._blog.settings;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.zone01._blog.media.MediaService;
import com.zone01._blog.user.User;
import com.zone01._blog.user.UserRepository;

@Service
public class SettingsService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final MediaService media;

    public SettingsService(UserRepository users, PasswordEncoder encoder, MediaService media) {
        this.users = users;
        this.encoder = encoder;
        this.media = media;
    }

    @Transactional
    public void changeEmail(User user, String newEmail) {
        if (users.existsByEmail(newEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
        user.setEmail(newEmail);
        users.save(user);
    }

    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword) {
        if (!encoder.matches(currentPassword, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
        }
        user.setPasswordHash(encoder.encode(newPassword));
        users.save(user);
    }

    @Transactional
    public void changeAvatar(User user, MultipartFile file) {
        String oldUrl = user.getAvatarUrl();
        String newUrl = media.store(file);
        user.setAvatarUrl(newUrl);
        users.save(user);
        if (oldUrl != null && !oldUrl.isBlank()) {
            media.delete(oldUrl);
        }
    }

    @Transactional
    public void deleteAccount(User user) {
        users.delete(user);
    }
}
