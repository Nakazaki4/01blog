package com.zone01._blog.subscription;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    private static Long requireSubscriberId(String principal) {
        if (principal == null || "anonymousUser".equals(principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return Long.parseLong(principal);
    }

    @PostMapping("/users/{id}/subscribe")
    public ResponseEntity<Void> subscribe(@AuthenticationPrincipal String principal, @PathVariable Long id) {
        subscriptionService.subscribe(requireSubscriberId(principal), id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/users/{id}/subscribe")
    public ResponseEntity<Void> unsubscribe(@AuthenticationPrincipal String principal, @PathVariable Long id) {
        subscriptionService.unsubscribe(requireSubscriberId(principal), id);
        return ResponseEntity.ok().build();
    }
}