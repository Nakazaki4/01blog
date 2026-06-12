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
    public void addLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        User actor = userRepository.findById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        User postAuthor = userRepository.findByPostId(postId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post author not found"));

        try {
            likeRepository.addPostLike(postId, userId);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Post already liked");
        }

        if (!actor.getId().equals(postAuthor.getId()) && !notificationRepo.existsByActorIdAndRecipientIdAndTypeAndPostId(
                actor.getId(),
                postAuthor.getId(),
                NotificationType.NEW_LIKE,
                post.getId()
        )) {
            Notification n = createNotification(post, actor, postAuthor);
            notificationRepo.save(n);
        }
    }

    public Notification createNotification(Post post, User actor, User recipient) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setActor(actor);
        n.setCreatedAt(Instant.now());
        n.setRead(false);
        n.setPost(post);
        n.setType(NotificationType.NEW_LIKE);

        return n;
    }

    @Transactional
    public void removeLike(Long postId, Long userId) {
        if (!postRepository.existsByIdAndDeletedFalse(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
        int deleted = likeRepository.removeLike(postId, userId);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Post not liked");
        }
    }
}
