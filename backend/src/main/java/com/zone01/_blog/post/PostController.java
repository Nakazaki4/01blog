package com.zone01._blog.post;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.zone01._blog.post.dto.PostResponse;

@RestController
@RequestMapping("/api")
public class PostController {

    private static final int MAX_POSTS = 20;
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    private static Long parseViewerId(String principal) {
        if (principal == null || "anonymousUser".equals(principal)) return -1L;
        return Long.parseLong(principal);
    }

    @GetMapping("/posts/feed")
    public ResponseEntity<?> feed(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > MAX_POSTS || size <= 0) {
            return ResponseEntity.badRequest().body("Size must be between 1 and " + MAX_POSTS);
        }

        Long viewerId = parseViewerId(userId);
        if (viewerId == -1L) {
            return ResponseEntity.ok(postService.getPublicFeed(page, size));
        }
        return ResponseEntity.ok(postService.getFeed(viewerId, page, size));
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable Long id,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(postService.getById(id, parseViewerId(userId)));
    }

    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<?> userPosts(
            @PathVariable Long userId,
            @AuthenticationPrincipal String principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > MAX_POSTS || size <= 0) {
            return ResponseEntity.badRequest().body("Size must be between 1 and " + MAX_POSTS);
        }
        return ResponseEntity.ok(postService.getByAuthor(userId, parseViewerId(principal), page, size));
    }

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> create(
            @AuthenticationPrincipal String userId,
            @RequestParam("description") String description,
            @RequestParam(value = "media", required = false) MultipartFile media) {
        PostResponse created = postService.create(Long.valueOf(userId), description, media);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping(value = "/posts/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> update(
            @PathVariable Long id,
            @AuthenticationPrincipal String userId,
            @RequestParam("description") String description,
            @RequestParam(value = "media", required = false) MultipartFile media) {
        return ResponseEntity.ok(postService.update(id, Long.valueOf(userId), description, media));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal String userId,
            Authentication auth) {
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        postService.delete(id, Long.valueOf(userId), isAdmin);
        return ResponseEntity.noContent().build();
    }
}
