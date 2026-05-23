package com.zone01._blog.like;

import com.zone01._blog.post.PostRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;

    public LikeService(LikeRepository likeRepository, PostRepository postRepository) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public void addLike(Long userId, Long postId) {
        if (!postRepository.existsByIdAndDeletedFalse(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
        try {
            likeRepository.addPostLike(postId, userId);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Post already liked");
        }
    }

    @Transactional
    public void removeLike(Long userId, Long postId) {
        if (!postRepository.existsByIdAndDeletedFalse(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
        int deleted = likeRepository.removeLike(postId, userId);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Post not liked");
        }
    }
}
