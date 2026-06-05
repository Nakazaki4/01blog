package com.zone01._blog.post;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("""
            SELECT p,
                    (SELECT COUNT(l) FROM Like l WHERE l.post.id = p.id) AS likeCount,
                    (SELECT COUNT(c) FROM Comment c WHERE c.post.id = p.id) AS commentCount,
                    (SELECT COUNT(l) > 0 FROM Like l WHERE l.post.id = p.id AND l.user.id = :userId) AS isLiked
            FROM Post p
            WHERE p.deleted = false
              AND (
                p.user.id = :userId
                OR p.user.id IN (
                    SELECT s.subscribedTo.id FROM Subscription s WHERE s.subscriber.id = :userId
                )
              )
            """)
    Page<Object[]> findFeedForUser(Long userId, Pageable pageable);

    @Query("""
            SELECT p,
                    (SELECT COUNT(l) FROM Like l WHERE l.post.id = p.id) AS likeCount,
                    (SELECT COUNT(c) FROM Comment c WHERE c.post.id = p.id) AS commentCount,
                    false AS isLiked
            FROM Post p
            WHERE p.deleted = false
            """)
    Page<Object[]> findPublicFeed(Pageable pageable);

    @Query("""
            SELECT p,
                    (SELECT COUNT(l) FROM Like l WHERE l.post.id = p.id) AS likeCount,
                    (SELECT COUNT(c) FROM Comment c WHERE c.post.id = p.id) AS commentCount,
                    (SELECT COUNT(l) > 0 FROM Like l WHERE l.post.id = p.id AND l.user.id = :viewerId) AS isLiked
            FROM Post p
            WHERE p.id = :postId AND p.deleted = false
            """)
    List<Object[]> findByIdWithCounts(Long postId, Long viewerId);

    @Query("""
            SELECT p,
                    (SELECT COUNT(l) FROM Like l WHERE l.post.id = p.id) AS likeCount,
                    (SELECT COUNT(c) FROM Comment c WHERE c.post.id = p.id) AS commentCount,
                    (SELECT COUNT(l) > 0 FROM Like l WHERE l.post.id = p.id AND l.user.id = :viewerId) AS isLiked
            FROM Post p
            WHERE p.user.id = :authorId AND p.deleted = false
            """)
    Page<Object[]> findByAuthorWithCounts(Long authorId, Long viewerId, Pageable pageable);

    List<Post> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId);

    boolean existsByIdAndDeletedFalse(Long id);
    Optional<Post> findById(Long id);
}
