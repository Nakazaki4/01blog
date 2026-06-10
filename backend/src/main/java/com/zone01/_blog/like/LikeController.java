package com.zone01._blog.like;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    private static Long parseViewerId(String principal) {
        if (principal == null || "anonymousUser".equals(principal)) {
            return -1L;
        }
        return Long.parseLong(principal);
    }

    @PostMapping("/posts/{post_id}/like")
    public ResponseEntity<Void> likePost(@AuthenticationPrincipal String userId, @PathVariable Long post_id) {
        likeService.addLike(post_id, parseViewerId(userId));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/posts/{post_id}/like")
    public ResponseEntity<Void> unlikePost(@AuthenticationPrincipal String userId, @PathVariable Long post_id) {
        likeService.removeLike(post_id, parseViewerId(userId));
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
