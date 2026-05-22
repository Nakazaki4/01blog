package com.zone01._blog.user;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.zone01._blog.post.Post;
import com.zone01._blog.post.PostRepository;
import com.zone01._blog.subscription.SubscriptionRepository;
import com.zone01._blog.user.dto.UserProfileResponse;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SubscriptionRepository subscriptionRepository;

    public UserService(UserRepository userRepository,
                       PostRepository postRepository,
                       SubscriptionRepository subscriptionRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Post> posts = postRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId);
        long subscribers = subscriptionRepository.countBySubscribedToId(userId);
        long following = subscriptionRepository.countBySubscriberId(userId);

        return new UserProfileResponse(
                user.getUsername(),
                user.getAvatarUrl(),
                user.getBio(),
                posts.toArray(new Post[0]),
                (int) subscribers,
                (int) following
        );
    }
}
