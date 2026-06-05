package com.zone01._blog.like;

import java.time.Instant;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.zone01._blog.notification.Notification;
import com.zone01._blog.notification.NotificationRepository;
import com.zone01._blog.notification.NotificationType;
import com.zone01._blog.post.Post;
import com.zone01._blog.post.PostRepository;
import com.zone01._blog.user.User;
import com.zone01._blog.user.UserRepository;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepo;

    public LikeService(LikeRepository likeRepository, PostRepository postRepository,
            UserRepository userRepo, NotificationRepository notifRepo
    ) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepo;
        this.notificationRepo = notifRepo;
    }

    @Transactional
    public void addLike(Long userId, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        User postAuthor = userRepository.findByPostId(postId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post author not found"));

        try {
            likeRepository.addPostLike(postId, userId);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Post already liked");
        }
        try {
            Notification n = createNotification(post, postAuthor);
            notificationRepo.save(n);
        } catch (DataIntegrityViolationException ignored) {
            return;
        }
    }

    public Notification createNotification(Post post, User recipient) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setActor(post.getUser());
        n.setCreatedAt(Instant.now());
        n.setRead(false);
        n.setPost(post);
        n.setType(NotificationType.NEW_LIKE);

        return n;
    }

    @Transactional
    public void removeLike(Long userId, Long postId) {
        if (!postRepository.existsByIdAndDeletedFalse(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
        int deleted = likeRepository.removeLike(postId, userId);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Post not liked");
        }
    }
}
