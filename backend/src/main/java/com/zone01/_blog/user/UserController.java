package com.zone01._blog.user;

import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    @GetMapping("/api/users/{id}")
    public ResponseEntity user(@PathVariable String id) {
        return null;
    }

    @GetMapping("/api/users/{id}/posts?page={page_number}")
    public ResponseEntity userFeed(@PathVariable String id, @PathVariable String page_number){
        return null;
    }

    @GetMapping("/api/users/{user_id}/subscribers?page={page_number}")
    public ResponseEntity subsribers(@PathVariable String user_id, @PathVariable String page_number) {
        return null;
    }

    @GetMapping("/api/users/{user_id}/subscriptions?page={page_number}")
    public ResponseEntity subscriptions(@PathVariable String user_id, @PathVariable String page_number) {
        return null;
    }

    @PostMapping("/api/users/{user_id}/subscribe")
    public ResponseEntity subscribe(@PathVariable String user_id){
        return null;
    }

    @DeleteMapping("/api/users/{user_id}/subscribe")
    public ResponseEntity unsubscribe(@PathVariable String user_id){
        return null;
    }

    @PatchMapping("/api/users/me")
    public ResponseEntity update(@PathVariable String user_id){
        return null;
    }
}
