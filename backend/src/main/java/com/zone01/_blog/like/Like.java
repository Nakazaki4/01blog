package com.zone01._blog.like;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.zone01._blog.post.Post;
import com.zone01._blog.user.User;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "likes", indexes = {
    @Index(name = "idx_likes_post_id", columnList = "post_id"),
    @Index(name = "idx_likes_post_user", columnList = "post_id, user_id")
})
public class Like {

    @EmbeddedId
    private LikeId id;

    @JoinColumn(name = "user_id", nullable = false)
    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @JoinColumn(name = "post_id", nullable = false)
    @MapsId("postId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post;
}
