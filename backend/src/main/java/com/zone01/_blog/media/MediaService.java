package com.zone01._blog.media;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaService.class);

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_SIZE = 10 * 1024 * 1024;
    private static final int MAX_DIMENSION = 8_000;
    private static final long MAX_PIXELS = 25_000_000L;

    private final RestClient http = RestClient.create();
    @Value("${app.supabase.url}")
    private String supabaseUrl;
    @Value("${app.supabase.service-key}")
    private String serviceKey;
    @Value("${app.supabase.bucket}")
    private String bucket;

    public String store(byte[] file, String fileType, String fileName) {
        validate(file, fileType);

        String ext = StringUtils.getFilenameExtension(fileName);
        String filename = UUID.randomUUID() + (ext != null ? "." + ext : "");
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + filename;

        http.post()
                .uri(uploadUrl)
                .header("apiKey", serviceKey)
                .header("Content-Type", fileType)
                .body(file)
                .retrieve()
                .toBodilessEntity();

        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + filename;
    }

    public void delete(String publicUrl) {
        if (publicUrl == null) return;
        String filename = publicUrl.substring(publicUrl.lastIndexOf('/') + 1);
        try {
            http.delete()
                    .uri(supabaseUrl + "/storage/v1/object/" + bucket + "/" + filename)
                    .header("apiKey", serviceKey)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to delete media {}: {}", filename, e.getMessage());
        }
    }

    private void validate(byte[] file, String fileType) {
        if (file.length > MAX_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File exceeds 10MB");
        }
        if (fileType == null || !ALLOWED_TYPES.contains(fileType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only images allowed (jpeg, png, webp, gif)");
        }
        verifyImageBytes(file);
    }

    private void verifyImageBytes(byte[] file) {
        BufferedImage image;
        try {
            image = ImageIO.read(new ByteArrayInputStream(file));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image");
        }
        if (image == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image");
        }
        int w = image.getWidth();
        int h = image.getHeight();
        if (w > MAX_DIMENSION || h > MAX_DIMENSION || (long) w * h > MAX_PIXELS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Image dimensions too large (max " + MAX_DIMENSION + "px per side)");
        }
    }
}
