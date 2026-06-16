package com.zone01._blog.media;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.UUID;

@Service
public class MediaService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_SIZE = 10 * 1024 * 1024;
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
        http.delete()
                .uri(supabaseUrl + "/storage/v1/object/" + bucket + "/" + filename)
                .header("apiKey", serviceKey)
                .retrieve()
                .toBodilessEntity();
    }

    private void validate(byte[] file, String fileType) {
        if (file.length > MAX_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File exceeds 10MB");
        }
        String contentType = fileType;
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only images allowed (jpeg, png, webp, gif)");
        }
    }
}