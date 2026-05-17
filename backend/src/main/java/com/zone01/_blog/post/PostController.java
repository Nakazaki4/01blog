package com.zone01._blog.post;

import com.zone01._blog.post.dto.CreatePostRequest;
import com.zone01._blog.post.dto.PostResponse;
import com.zone01._blog.post.dto.UpdatePostRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class PostController {
    private static final int MAX_POSTS = 20;
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/posts/feed")
    public ResponseEntity<?> feed(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > MAX_POSTS || size <= 0) {
            return ResponseEntity.badRequest().body("Size must be between 1 and " + MAX_POSTS);
        }

        return ResponseEntity.ok(postService.getFeed(Long.parseLong(userId), page, size));
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable Long id,
            @AuthenticationPrincipal String userId) {
        Long viewerId = userId != null ? Long.parseLong(userId) : -1L;
        return ResponseEntity.ok(postService.getById(id, viewerId));
    }

    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<?> userPosts(
            @PathVariable Long userId,
            @AuthenticationPrincipal String viewerIdStr,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > MAX_POSTS || size <= 0) {
            return ResponseEntity.badRequest().body("Size must be between 1 and " + MAX_POSTS);
        }
        Long viewerId = viewerIdStr != null ? Long.parseLong(viewerIdStr) : -1L;
        return ResponseEntity.ok(postService.getByAuthor(userId, viewerId, page, size));
    }

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> create(
            @AuthenticationPrincipal String userId,
            @RequestParam("description") String description,
            @RequestParam(value = "media", required = false) MultipartFile media) {
        PostResponse created = postService.create(Long.parseLong(userId), description, media);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping(value = "/posts/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> update(
            @PathVariable Long id,
            @AuthenticationPrincipal String userId,
            @RequestParam("description") String description,
            @RequestParam(value = "media", required = false) MultipartFile media) {
        return ResponseEntity.ok(postService.update(id, Long.parseLong(userId), description, media));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal String userId,
            Authentication auth) {
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        postService.delete(id, Long.parseLong(userId), isAdmin);
        return ResponseEntity.noContent().build();
    }
}
