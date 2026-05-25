package com.zone01._blog.subscription;

import com.zone01._blog.user.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
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
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already subscribed");
        }
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