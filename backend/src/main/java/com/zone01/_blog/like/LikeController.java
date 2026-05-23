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

    @PostMapping("/posts/{id}/like")
    public ResponseEntity<Void> likePost(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        likeService.addLike(userId, id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/posts/{id}/like")
    public ResponseEntity<Void> unlikePost(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        likeService.removeLike(userId, id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
