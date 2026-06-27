package com.zone01._blog.comment;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.zone01._blog.comment.dto.CommentResponse;
import com.zone01._blog.notification.Notification;
import com.zone01._blog.notification.NotificationRepository;
import com.zone01._blog.notification.NotificationType;
import com.zone01._blog.post.Post;
import com.zone01._blog.post.PostRepository;
import com.zone01._blog.post.dto.UserPost;
import com.zone01._blog.user.User;
import com.zone01._blog.user.UserRepository;

@Service
public class CommentService {

    private final CommentRepository commentRepo;
    private final PostRepository postRepo;
    private final UserRepository userRepo;
    private final NotificationRepository notificationRepo;
    private static final int MAX_CONTENT_LENGTH = 250;
    private static final int MIN_CONTENT_LENGTH = 12;

    public CommentService(CommentRepository commentRepository, PostRepository postRepo,
            UserRepository userRepo, NotificationRepository notifRepo) {
        this.commentRepo = commentRepository;
        this.postRepo = postRepo;
        this.userRepo = userRepo;
        this.notificationRepo = notifRepo;
    }

    public CommentResponse addComment(Long postId, Long userId, String content) {
        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content is required");
        }

        if (content.length() < MIN_CONTENT_LENGTH) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least " + MIN_CONTENT_LENGTH + " characters allowed");
        }

        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At most " + MAX_CONTENT_LENGTH + " characters allowed");
        }

        Post post = this.postRepo.findById(postId).filter(p -> !p.isDeleted()).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        User author = this.userRepo.findById(userId).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")
        );

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setContent(content);
        comment.setUser(author);
        Comment saved = this.commentRepo.save(comment);

        User postAuthor = userRepo.findByPostId(postId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post owner not found")
        );
        if (!postAuthor.getId().equals(userId)) {
            Notification n = createNotification(post, postAuthor, author);
            notificationRepo.save(n);
        }

        return toResponse(saved);
    }

    public Notification createNotification(Post post, User recipient, User actor) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setActor(actor);
        n.setCreatedAt(Instant.now());
        n.setRead(false);
        n.setPost(post);
        n.setType(NotificationType.NEW_COMMENT);

        return n;
    }

    public Page<CommentResponse> listForPost(Long postId, int page, int size) {
        if (!postRepo.existsByIdAndDeletedFalseAndHiddenFalse(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
        return commentRepo.findByPostIdOrderByCreatedAtDesc(postId, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    private CommentResponse toResponse(Comment comment) {
        User author = comment.getUser();
        return new CommentResponse(
                comment.getId(),
                new UserPost(author.getId(), author.getUsername(), author.getAvatarUrl()),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
