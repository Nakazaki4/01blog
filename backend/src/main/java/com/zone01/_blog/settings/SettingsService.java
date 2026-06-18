package com.zone01._blog.settings;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.zone01._blog.media.MediaService;
import com.zone01._blog.user.Role;
import com.zone01._blog.user.User;
import com.zone01._blog.user.UserRepository;

import net.coobird.thumbnailator.Thumbnails;

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
        if (file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Image is required"
            );
        }

        byte[] webp;
        try {
            BufferedImage image
                    = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new IllegalArgumentException("Invalid image");
            }
            webp = convertToWebp(file);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid image"
            );
        }

        String oldUrl = user.getAvatarUrl();
        String newUrl = media.store(webp, file.getContentType(), file.getOriginalFilename());
        try {
            user.setAvatarUrl(newUrl);
            users.save(user);
            if (oldUrl != null && !oldUrl.isBlank()) {
                media.delete(oldUrl);
            }
        } catch (Exception e) {
            media.delete(newUrl);
            throw e;
        }
    }

    private byte[] convertToWebp(MultipartFile file) throws Exception {
        if ("image/webp".equals(file.getContentType())) {
            return file.getBytes();
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Thumbnails.of(new ByteArrayInputStream(file.getBytes())).scale(1.0).outputFormat("webp").toOutputStream(output);
        return output.toByteArray();
    }

    @Transactional
    public void deleteAccount(User user) {
        if (user.getRole() == Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin accounts cannot be deleted");
        }
        users.delete(user);
    }
}
