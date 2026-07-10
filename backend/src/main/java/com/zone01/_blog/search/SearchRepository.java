package com.zone01._blog.search;

import com.zone01._blog.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SearchRepository extends JpaRepository<User, Long> {
    @Query("""
    SELECT u FROM User u
    WHERE u.banned = false
      AND LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
    ORDER BY
      CASE WHEN LOWER(u.username) = LOWER(:keyword) THEN 0
           WHEN LOWER(u.username) LIKE LOWER(CONCAT(:keyword, '%')) THEN 1
           ELSE 2 END,
      u.username ASC
    """)
    List<User> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
