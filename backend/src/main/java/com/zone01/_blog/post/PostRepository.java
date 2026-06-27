package com.zone01._blog.post;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zone01._blog.post.dto.FeedPost;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
    SELECT new com.zone01._blog.post.dto.FeedPost(
        p.id,
        new com.zone01._blog.post.dto.UserPost(p.user.id, p.user.username, p.user.avatarUrl),
        p.createdAt,
        SUBSTRING(p.description, 1, 180),
        (SELECT COUNT(l) FROM Like l WHERE l.post.id = p.id),
        (SELECT COUNT(c) FROM Comment c WHERE c.post.id = p.id),
        CASE WHEN (
            SELECT COUNT(l)
            FROM Like l
            WHERE l.post.id = p.id
              AND l.user.id = :userId
        ) > 0
        THEN true
        ELSE false
        END
    )
    FROM Post p
    WHERE p.deleted = false
      AND p.hidden = false
      AND (
        p.user.id = :userId
        OR p.user.id IN (
            SELECT s.subscribedTo.id FROM Subscription s WHERE s.subscriber.id = :userId
        )
      )
    ORDER BY p.createdAt DESC
""")
    Page<FeedPost> findFeedForUser(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        SELECT new com.zone01._blog.post.dto.FeedPost(
                p.id,
                new com.zone01._blog.post.dto.UserPost(p.user.id, p.user.username, p.user.avatarUrl),
                p.createdAt,
                SUBSTRING(p.description, 1, 180),
                (SELECT COUNT(l) FROM Like l WHERE l.post.id = p.id),
                (SELECT COUNT(c) FROM Comment c WHERE c.post.id = p.id),
                false
    )
    FROM Post p
    WHERE p.deleted = false
      AND p.hidden = false
    ORDER BY p.createdAt DESC
            """)
    Page<FeedPost> findPublicFeed(Pageable pageable);

    @Query("""
            SELECT p,
                    (SELECT COUNT(l) FROM Like l WHERE l.post.id = p.id) AS likeCount,
                    (SELECT COUNT(c) FROM Comment c WHERE c.post.id = p.id) AS commentCount,
                    (SELECT COUNT(l) > 0 FROM Like l WHERE l.post.id = p.id AND l.user.id = :viewerId) AS isLiked
            FROM Post p
            WHERE p.id = :postId AND p.deleted = false AND p.hidden = false
            """)
    List<Object[]> findByIdWithCounts(@Param("postId") Long postId, @Param("viewerId") Long viewerId);

    @Query("""
            SELECT new com.zone01._blog.post.dto.FeedPost(
                p.id,
                new com.zone01._blog.post.dto.UserPost(p.user.id, p.user.username, p.user.avatarUrl),
                p.createdAt,
                SUBSTRING(p.description, 1, 180),
                (SELECT COUNT(l) FROM Like l WHERE l.post.id = p.id),
                (SELECT COUNT(c) FROM Comment c WHERE c.post.id = p.id),
                CASE WHEN (
                        SELECT COUNT(l)
                        FROM Like l
                        WHERE l.post.id = p.id
                        AND l.user.id = :viewerId
                ) > 0
                THEN true
                ELSE false
                END
            )
            FROM Post p
            WHERE p.user.id = :authorId AND p.deleted = false AND p.hidden = false
            ORDER BY p.createdAt DESC
            """)
    Page<FeedPost> findByAuthorWithCounts(@Param("authorId") Long authorId, @Param("viewerId") Long viewerId, Pageable pageable);

    @Query("""
            SELECT p,
                    (SELECT COUNT(l) FROM Like l WHERE l.post.id = p.id) AS likeCount,
                    (SELECT COUNT(c) FROM Comment c WHERE c.post.id = p.id) AS commentCount,
                    (SELECT COUNT(r) FROM Report r WHERE r.reportedUser.id = p.user.id) AS reportCount
            FROM Post p
            WHERE p.deleted = false
            ORDER BY p.createdAt DESC
            """)
    Page<Object[]> findAllWithAdminCounts(Pageable pageable);

    List<Post> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId);

    long countByUserId(Long userId);

    boolean existsByIdAndDeletedFalseAndHiddenFalse(Long id);

    Optional<Post> findById(Long id);

    @Modifying
    @Query("UPDATE Post p SET p.hidden = :state WHERE p.id = :postId")
    int setHidden(@Param("postId") Long postId, @Param("state") boolean state);
}
