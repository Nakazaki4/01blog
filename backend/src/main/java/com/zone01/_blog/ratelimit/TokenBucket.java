package com.zone01._blog.ratelimit;

public class TokenBucket {

    private final int capacity;
    private final int refillRate;

    private int tokens;
    private long lastRefillTimestamp;

    public TokenBucket(int capacity, int refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.tokens = capacity;
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    public synchronized boolean tryConsume() {
        refill();

        if (tokens > 0) {
            tokens--;
            return true;
        }

        return false;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefillTimestamp;
        long msPerToken = 1000L / refillRate;

        long tokensToAdd = elapsed / msPerToken;
        if (tokensToAdd > 0) {
            tokens = (int) Math.min(capacity ,tokensToAdd + tokens);
            lastRefillTimestamp=System.currentTimeMillis();
        }
    }
}
