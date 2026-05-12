package com.zone01._blog.like;

import com.zone01._blog.post.Post;
import com.zone01._blog.user.User;

import jakarta.persistence.*;

@Entity
@Table(name="likes")
public class Like {
    @EmbeddedId
    private LikeId id;

    @JoinColumn(name="user_id", nullable = false)
    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional=false)
    private User user;

    @JoinColumn(name="post_id", nullable = false)
    @MapsId("postId")
    @ManyToOne(fetch = FetchType.LAZY, optional=false)
    private Post post;
}