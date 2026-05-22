package com.zone01._blog.subscription;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, SubscriptionId> {
    long countBySubscribedToId(Long userId);
    long countBySubscriberId(Long userId);
}
