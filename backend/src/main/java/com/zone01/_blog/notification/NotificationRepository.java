package com.zone01._blog.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    long countByRecipientIdAndIsReadFalse(Long recipientId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id AND n.recipient.id = :recipientId AND n.isRead = false")
    int markAsRead(@Param("id") Long id, @Param("recipientId") Long recipientId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.id = :recipientId AND n.isRead = false")
    int markAllAsRead(@Param("recipientId") Long recipientId);

    boolean existsByActorIdAndRecipientIdAndType(Long actorId, Long recipientId, NotificationType type);

    boolean existsByActorIdAndRecipientIdAndTypeAndPostId(
            Long actorId,
            Long recipientId,
            NotificationType type,
            Long postId
    );
}
