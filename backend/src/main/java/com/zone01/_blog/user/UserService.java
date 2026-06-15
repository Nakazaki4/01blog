package com.zone01._blog.user;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.zone01._blog.post.PostService;
import com.zone01._blog.post.dto.PostResponse;
import com.zone01._blog.subscription.SubscriptionRepository;
import com.zone01._blog.user.dto.UserProfileResponse;

@Service
public class UserService {

    private static final int PROFILE_POSTS_PAGE_SIZE = 20;

    private final UserRepository userRepository;
    private final PostService postService;
    private final SubscriptionRepository subscriptionRepository;

    public UserService(UserRepository userRepository,
            PostService postService,
            SubscriptionRepository subscriptionRepository) {
        this.userRepository = userRepository;
        this.postService = postService;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId, Long viewerId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<PostResponse> posts = postService.getByAuthor(userId, viewerId, 0, PROFILE_POSTS_PAGE_SIZE);
        long subscribers = subscriptionRepository.countBySubscribedToId(userId);
        long following = subscriptionRepository.countBySubscriberId(userId);
        boolean isSubscribed = viewerId > 0
                && !viewerId.equals(userId)
                && subscriptionRepository.existsBySubscriberIdAndSubscribedToId(viewerId, userId);

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getAvatarUrl(),
                user.getBio(),
                posts,
                (int) subscribers,
                (int) following,
                isSubscribed
        );
    }
}
