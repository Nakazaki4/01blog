package com.zone01._blog.user;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username,
            String email,
            Pageable pageable
    );

    @Query("SELECT p.user FROM Post p WHERE p.id = :postId")
    Optional<User> findByPostId(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE User u SET u.banned = :state WHERE u.id = :userId ")
    int switchBannedToTrue(@Param("userId") Long userId, @Param("state") boolean flag);
}
