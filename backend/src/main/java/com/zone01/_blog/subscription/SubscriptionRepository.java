package com.zone01._blog.subscription;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zone01._blog.user.User;

public interface SubscriptionRepository extends JpaRepository<Subscription, SubscriptionId> {

    long countBySubscribedToId(Long userId);

    long countBySubscriberId(Long userId);

    boolean existsBySubscriberIdAndSubscribedToId(Long subscriberId, Long subscribedToId);

    @Modifying
    @Query(value = """
        INSERT INTO subscriptions (created_at, subscriber_id, subscribed_to_id) VALUES (now(), :subscriberId, :subscribedToId)
    """, nativeQuery = true)
    int addSubscription(@Param("subscriberId") Long subscriberId, @Param("subscribedToId") Long subscribedToId);

    @Modifying
    @Query(value = """
        DELETE FROM subscriptions WHERE subscriber_id = :subscriberId AND subscribed_to_id = :subscribedToId
    """, nativeQuery = true)
    int removeSubscription(@Param("subscriberId") Long subscriberId, @Param("subscribedToId") Long subscribedToId);

    @Query("SELECT s.subscriber FROM Subscription s WHERE s.subscribedTo.id = :subscribedToId")
    List<User> findFollowersByFollowedId(@Param("subscribedToId") Long subscribedToId);
}
