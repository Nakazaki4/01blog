package com.zone01._blog.notification;

import com.zone01._blog.notification.dto.NotificationResponse;
import com.zone01._blog.post.Post;
import com.zone01._blog.post.dto.UserPost;
import com.zone01._blog.user.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> list(Long recipientId, int page, int size) {
        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(recipientId, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long recipientId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
    }

    @Transactional
    public void markRead(Long recipientId, Long notificationId) {
        int updated = notificationRepository.markAsRead(notificationId, recipientId);
        if (updated == 0) {
            // Either it doesn't exist, isn't ours, or is already read — only the
            // first two are real errors. Treat as no-op when already read.
            if (!notificationRepository.existsById(notificationId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found");
            }
        }
    }

    @Transactional
    public int markAllRead(Long recipientId) {
        return notificationRepository.markAllAsRead(recipientId);
    }

    private NotificationResponse toResponse(Notification n) {
        User actor = n.getActor();
        Post post = n.getPost();
        return new NotificationResponse(
                n.getId(),
                new UserPost(actor.getId(), actor.getUsername(), actor.getAvatarUrl()),
                n.getType(),
                post != null ? post.getId() : null,
                n.isRead(),
                n.getCreatedAt()
        );
    }
}