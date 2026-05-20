package com.zone01._blog.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.zone01._blog.media.MediaService;
import com.zone01._blog.post.dto.PostResponse;
import com.zone01._blog.post.dto.UserPost;
import com.zone01._blog.user.User;
import com.zone01._blog.user.UserRepository;

@Service
public class PostService {
    private final PostRepository postRepo;
    private final UserRepository userRepo;
    private final MediaService mediaService;

    public PostService(PostRepository postRepo, UserRepository userRepo, MediaService mediaService) {
        this.postRepo = postRepo;
        this.userRepo = userRepo;
        this.mediaService = mediaService;
    }

    public Page<PostResponse> getFeed(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepo.findFeedForUser(userId, pageable).map(this::toDto);
    }

    public Page<PostResponse> getPublicFeed(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepo.findPublicFeed(pageable).map(this::toDto);
    }

    public PostResponse getById(Long postId, Long viewerId) {
        return postRepo.findByIdWithCounts(postId, viewerId).stream()
                .findFirst()
                .map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    public Page<PostResponse> getByAuthor(Long authorId, Long viewerId, int page, int size) {
        if (!userRepo.existsById(authorId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepo.findByAuthorWithCounts(authorId, viewerId, pageable).map(this::toDto);
    }

    public PostResponse create(Long authorId, String description, MultipartFile media) {
        if (description == null || description.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description is required");
        }
        User author = userRepo.findById(authorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        String mediaUrl = mediaService.store(media);

        Post post = new Post();
        post.setUser(author);
        post.setDescription(description);
        post.setMediaUrl(mediaUrl);
        post.setMediaType(mediaUrl != null && !mediaUrl.isBlank() ? MediaType.IMAGE : null);
        Post saved = postRepo.save(post);

        return new PostResponse(
                saved.getId(),
                new UserPost(author.getId(), author.getUsername(), author.getAvatarUrl()),
                saved.getDescription(),
                saved.getMediaUrl(),
                0,
                0,
                false,
                saved.getCreatedAt()
        );
    }

    public PostResponse update(Long postId, Long requesterId, String description, MultipartFile media) {
        Post post = postRepo.findById(postId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (!post.getUser().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not the post owner");
        }
        if (description == null || description.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description is required");
        }

        post.setDescription(description);

        if (media != null && !media.isEmpty()) {
            if (post.getMediaUrl() != null) {
                mediaService.delete(post.getMediaUrl());
            }
            post.setMediaUrl(mediaService.store(media));
        }

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
        mediaService.delete(post.getMediaUrl());
        postRepo.save(post);
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
                p.getMediaUrl(),
                (int) likeCount,
                (int) commentCount,
                isLiked,
                p.getCreatedAt()
        );
    }
}
