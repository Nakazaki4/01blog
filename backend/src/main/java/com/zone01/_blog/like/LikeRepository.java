package com.zone01._blog.like;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikeRepository extends JpaRepository<Like, LikeId> {

    @Modifying
    @Query(value = """
        INSERT INTO likes (user_id, post_id) VALUES (:userId, :postId)
    """, nativeQuery = true)
    int addPostLike(@Param("postId") Long postId, @Param("userId") Long userId);

    @Modifying
    @Query(value = """
        DELETE FROM likes WHERE user_id = :userId AND post_id = :postId
    """, nativeQuery = true)
    int removeLike(@Param("postId") Long postId, @Param("userId") Long userId);
}
