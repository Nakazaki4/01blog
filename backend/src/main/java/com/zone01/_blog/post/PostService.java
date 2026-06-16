package com.zone01._blog.post;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.zone01._blog.media.MediaService;
import com.zone01._blog.notification.Notification;
import com.zone01._blog.notification.NotificationRepository;
import com.zone01._blog.notification.NotificationType;
import com.zone01._blog.post.dto.PostResponse;
import com.zone01._blog.post.dto.UserPost;
import com.zone01._blog.subscription.SubscriptionRepository;
import com.zone01._blog.user.User;
import com.zone01._blog.user.UserRepository;

@Service
public class PostService {

    private static final Pattern MARKDOWN_IMAGE = Pattern.compile("!\\[[^\\]]*\\]\\(([^)\\s]+)\\)");

    private final PostRepository postRepo;
    private final UserRepository userRepo;
    private final MediaService mediaService;
    private final NotificationRepository notificationRepo;
    private final SubscriptionRepository subscriptionRepo;

    public PostService(PostRepository postRepo, UserRepository userRepo, MediaService mediaService,
            NotificationRepository notifRepo, SubscriptionRepository subscriptionRepo) {
        this.postRepo = postRepo;
        this.userRepo = userRepo;
        this.mediaService = mediaService;
        this.notificationRepo = notifRepo;
        this.subscriptionRepo = subscriptionRepo;
    }

    public List<PostResponse> getFeed(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepo.findFeedForUser(userId, pageable).map(this::toDto).toList();
    }

    public List<PostResponse> getPublicFeed(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepo.findPublicFeed(pageable).map(this::toDto).toList();
    }

    public PostResponse getById(Long postId, Long viewerId) {
        return postRepo.findByIdWithCounts(postId, viewerId).stream()
                .findFirst()
                .map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    public List<PostResponse> getByAuthor(Long authorId, Long viewerId, int page, int size) {
        if (!userRepo.existsById(authorId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepo.findByAuthorWithCounts(authorId, viewerId, pageable).map(this::toDto).toList();
    }

    public PostResponse create(Long authorId, String description) {
        if (description == null || description.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description is required");
        }
        if (description.length() < 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least 2000 characters required");
        }
        if (description.length() > 10000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At most 10000 characters allowed");
        }
        User author = userRepo.findById(authorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Post post = new Post();
        post.setUser(author);
        post.setDescription(description);
        Post saved = postRepo.save(post);

        // notify all subscribers
        List<User> subscribers = subscriptionRepo.findFollowersByFollowedId(authorId);
        List<Notification> notifications = subscribers.stream()
                .map(subscriber -> createNotification(post, subscriber))
                .toList();

        notificationRepo.saveAll(notifications);

        return new PostResponse(
                saved.getId(),
                new UserPost(author.getId(), author.getUsername(), author.getAvatarUrl()),
                saved.getDescription(),
                0,
                0,
                false,
                saved.getCreatedAt()
        );
    }

    public Notification createNotification(Post post, User recipient) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setActor(post.getUser());
        n.setCreatedAt(Instant.now());
        n.setRead(false);
        n.setPost(post);
        n.setType(NotificationType.NEW_POST);

        return n;
    }

    public PostResponse update(Long postId, Long requesterId, String description) {
        Post post = postRepo.findById(postId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (!post.getUser().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not the post owner");
        }
        if (description == null || description.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description is required");
        }
        if (description.length() < 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least 2000 characters required");
        }
        if (description.length() > 10000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At most 10000 characters allowed");
        }

        List<String> previousImages = extractImageUrls(post.getDescription());
        List<String> nextImages = extractImageUrls(description);
        for (String url : previousImages) {
            if (!nextImages.contains(url)) {
                mediaService.delete(url);
            }
        }

        post.setDescription(description);
        postRepo.save(post);
        return getById(postId, requesterId);
    }

    public void delete(Long postId, Long requesterId, boolean isAdmin) {
        Post post = postRepo.findById(postId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (!isAdmin && !post.getUser().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not the post owner");
        }
        post.setDeleted(true);
        for (String url : extractImageUrls(post.getDescription())) {
            mediaService.delete(url);
        }
        postRepo.save(post);
    }

    private List<String> extractImageUrls(String markdown) {
        List<String> urls = new ArrayList<>();
        if (markdown == null) {
            return urls;
        }
        Matcher m = MARKDOWN_IMAGE.matcher(markdown);
        while (m.find()) {
            urls.add(m.group(1));
        }
        return urls;
    }

    public String storeImage(MultipartFile file){
        String supabaseUrl;
        try{
        supabaseUrl = mediaService.store(file.getBytes(), file.getContentType(), file.getOriginalFilename());
        }catch(IOException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while processing image");
        }
        return supabaseUrl;
    }

    private PostResponse toDto(Object[] row) {
        Post p = (Post) row[0];
        long likeCount = ((Number) row[1]).longValue();
        long commentCount = ((Number) row[2]).longValue();
        boolean isLiked = (boolean) row[3];

        return new PostResponse(
                p.getId(),
                new UserPost(
                        p.getUser().getId(),
                        p.getUser().getUsername(),
                        p.getUser().getAvatarUrl()
                ),
                p.getDescription(),
                (int) likeCount,
                (int) commentCount,
                isLiked,
                p.getCreatedAt()
        );
    }
}
