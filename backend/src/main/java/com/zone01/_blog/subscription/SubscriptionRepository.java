package com.zone01._blog.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository extends JpaRepository<Subscription, SubscriptionId> {
    long countBySubscribedToId(Long userId);
    long countBySubscriberId(Long userId);
    boolean existsBySubscriberIdAndSubscribedToId(Long subscriberId, Long subscribedToId);

    @Modifying
    @Query(value = """
        INSERT INTO subscriptions (subscriber_id, subscribed_to_id) VALUES (:subscriberId, :subscribedToId)
    """, nativeQuery = true)
    int addSubscription(@Param("subscriberId") Long subscriberId, @Param("subscribedToId") Long subscribedToId);

    @Modifying
    @Query(value = """
        DELETE FROM subscriptions WHERE subscriber_id = :subscriberId AND subscribed_to_id = :subscribedToId
    """, nativeQuery = true)
    int removeSubscription(@Param("subscriberId") Long subscriberId, @Param("subscribedToId") Long subscribedToId);
}