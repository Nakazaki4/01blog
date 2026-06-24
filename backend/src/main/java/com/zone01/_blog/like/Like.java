package com.zone01._blog.like;

import com.zone01._blog.post.Post;
import com.zone01._blog.user.User;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name="likes")
public class Like {
    @EmbeddedId
    private LikeId id;

    @JoinColumn(name="user_id", nullable = false)
    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional=false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @JoinColumn(name="post_id", nullable = false)
    @MapsId("postId")
    @ManyToOne(fetch = FetchType.LAZY, optional=false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post;
}