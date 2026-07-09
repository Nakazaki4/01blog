package com.zone01._blog.ratelimit;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_SECOND = 6;
    private static final int REFILL_RATE_PER_SECOND = 2;
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
            throws ServletException, IOException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = httpRequest.getRemoteAddr();
        buckets.computeIfAbsent(clientIp, ip -> new TokenBucket(MAX_REQUESTS_PER_SECOND,
            REFILL_RATE_PER_SECOND));

        if (!buckets.get(clientIp).tryConsume()) {
            httpResponse.setStatus(429);
            httpResponse.getWriter().write("Too many requests. Please try again later.");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
