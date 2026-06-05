package com.zone01._blog.subscription;

import java.time.Instant;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.zone01._blog.notification.Notification;
import com.zone01._blog.notification.NotificationRepository;
import com.zone01._blog.notification.NotificationType;
import com.zone01._blog.user.User;
import com.zone01._blog.user.UserRepository;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepo;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, UserRepository userRepository,
            NotificationRepository notifRepo
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.notificationRepo = notifRepo;
    }

    @Transactional
    public void subscribe(Long subscriberId, Long subscribedToId) {
        if (subscriberId.equals(subscribedToId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot subscribe to yourself");
        }
        if (!userRepository.existsById(subscribedToId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        try {
            subscriptionRepository.addSubscription(subscriberId, subscribedToId);
            User recipient = userRepository.findById(subscribedToId).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
            );
            User subscriber = userRepository.findById(subscriberId).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
            );

            boolean exists = notificationRepo.existsByActorIdAndRecipientIdAndType(
                    subscriber.getId(), recipient.getId(), NotificationType.NEW_SUBSCRIBER
            );
            if (!exists) {
                Notification n = createNotification(subscriber, recipient);
                notificationRepo.save(n);
            }
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already subscribed");
        }
    }

    public Notification createNotification(User subscriber, User recipient) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setActor(subscriber);
        n.setCreatedAt(Instant.now());
        n.setRead(false);
        n.setPost(null);
        n.setType(NotificationType.NEW_SUBSCRIBER);

        return n;
    }

    @Transactional
    public void unsubscribe(Long subscriberId, Long subscribedToId) {
        if (!userRepository.existsById(subscribedToId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        int deleted = subscriptionRepository.removeSubscription(subscriberId, subscribedToId);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Not subscribed");
        }
    }
}
