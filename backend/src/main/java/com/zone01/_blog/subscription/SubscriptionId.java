package com.zone01._blog.subscription;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SubscriptionId implements Serializable {
    @Column(name = "subscriber_id")
    private Long subscriberId;

    @Column(name = "subscribed_to_id")
    private Long subscribedToId;

    public SubscriptionId() {
    }

    public SubscriptionId(Long subscriberId, Long subscribedToId) {
        this.subscriberId = subscriberId;
        this.subscribedToId = subscribedToId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscriptionId that)) return false;
        return Objects.equals(subscriberId, that.subscriberId)
                && Objects.equals(subscribedToId, that.subscribedToId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriberId, subscribedToId);
    }
}
