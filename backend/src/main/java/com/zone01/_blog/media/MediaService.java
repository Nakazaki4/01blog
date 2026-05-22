package com.zone01._blog.media;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_SIZE = 10 * 1024 * 1024;

    @Value("${app.supabase.url}")
    private String supabaseUrl;

    @Value("${app.supabase.service-key}")
    private String serviceKey;

    @Value("${app.supabase.bucket}")
    private String bucket;

    private final RestClient http = RestClient.create();

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        validate(file);

        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + (ext != null ? "." + ext : "");
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + filename;

        try {
            http.post()
                    .uri(uploadUrl)
                    .header("apiKey", serviceKey)
                    .header("Content-Type", file.getContentType())
                    .body(file.getBytes())
                    .retrieve()
                    .toBodilessEntity();
        } catch (IOException e) {
            throw new RuntimeException("Upload failed", e);
        }

        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + filename;
    }

    public void delete(String publicUrl) {
        if (publicUrl == null) return;
        String filename = publicUrl.substring(publicUrl.lastIndexOf('/') + 1);
        http.delete()
                .uri(supabaseUrl + "/storage/v1/object/" + bucket + "/" + filename)
                .header("apiKey", serviceKey)
                .retrieve()
                .toBodilessEntity();
    }

    private void validate(MultipartFile file) {
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("File exceeds 10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only images allowed (jpeg, png, webp, gif)");
        }
    }
}