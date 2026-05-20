package com.zone01._blog.config;

import com.zone01._blog.post.Post;
import com.zone01._blog.post.PostRepository;
import com.zone01._blog.user.Role;
import com.zone01._blog.user.User;
import com.zone01._blog.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(2)
@RequiredArgsConstructor
public class PostSeeder implements CommandLineRunner {
    private final PostRepository postRepo;
    private final UserRepository userRepo;

    private static final List<String> SAMPLE_DESCRIPTIONS = List.of(
            "Just shipped the post feature backend. JPA aggregate queries are surprisingly ergonomic once you accept the Object[] result shape.",
            "Spent the morning untangling Spring Security filter chains. The trick: order matters and silent permitAll fallthrough hides bugs.",
            "Hot take: soft deletes beat hard deletes in social apps. Audit trails, undelete UX, and you stop worrying about cascade rules.",
            "Reading about JWT vs session cookies again. Both are fine. Pick the one whose failure mode you understand.",
            "Postgres 18 changed the docker volume layout. Lost an hour to that today. Moral: pin major versions in compose files.",
            "TIL @AuthenticationPrincipal can be a String when you stuff the user id into the JWT subject. No more custom argument resolvers.",
            "Refactor of the day: extracted a single PostResponse DTO. The previous name (PageResponse) was lying about what it represented.",
            "Considering whether to add full-text search now or wait until users complain. Probably the latter.",
            "Wrote a feed query that returns posts + like count + comment count + isLiked in one shot. Hibernate is fine if you let it be.",
            "Image uploads next. Going with Supabase storage so I don't have to babysit S3 buckets on the weekend."
    );

    @Override
    public void run(String... args) {
        if (postRepo.count() > 0) return;

        User admin = userRepo.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN)
                .findFirst()
                .orElse(null);

        if (admin == null) {
            System.out.println("PostSeeder: no admin user found, skipping");
            return;
        }

        for (String description : SAMPLE_DESCRIPTIONS) {
            Post post = new Post();
            post.setUser(admin);
            post.setDescription(description);
            postRepo.save(post);
        }

        System.out.println("PostSeeder: seeded " + SAMPLE_DESCRIPTIONS.size() + " posts for " + admin.getUsername());
    }
}
