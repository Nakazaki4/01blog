package com.zone01._blog.comment;

import com.zone01._blog.comment.dto.CommentRequest;
import com.zone01._blog.comment.dto.CommentResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CommentController {
    private static final int MAX_COMMENTS = 50;
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    private static Long parseId(String principal) {
        if (principal == null || "anonymousUser".equals(principal)) return -1L;
        return Long.parseLong(principal);
    }

    @GetMapping("/posts/{id}/comments")
    public ResponseEntity<?> listComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (size > MAX_COMMENTS || size <= 0) {
            return ResponseEntity.badRequest().body("Size must be between 1 and " + MAX_COMMENTS);
        }
        Page<CommentResponse> result = commentService.listForPost(id, page, size);
        return ResponseEntity.ok(result.toList());
    }

    @PostMapping("/posts/{id}/comments")
    public ResponseEntity<CommentResponse> postComment(
            @AuthenticationPrincipal String userId,
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest req) {
        CommentResponse created = commentService.addComment(id, parseId(userId), req.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/posts/{id}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal String userId,
            @PathVariable Long id,
            @PathVariable Long commentId,
            Authentication auth) {
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        commentService.deleteComment(id, commentId, parseId(userId), isAdmin);
        return ResponseEntity.noContent().build();
    }
}
