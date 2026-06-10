package com.zone01._blog.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.zone01._blog.post.Post;
import com.zone01._blog.post.PostRepository;
import com.zone01._blog.report.Report;
import com.zone01._blog.report.ReportRepository;
import com.zone01._blog.user.User;
import com.zone01._blog.user.UserRepository;

@Service
public class AdminService {

    private final AdminRepository adminRepo;
    private final UserRepository userRepo;
    private final PostRepository postRepo;
    private final ReportRepository reportRepo;

    public AdminService(AdminRepository adminRepo, UserRepository userRepo,
            PostRepository postRepo, ReportRepository reportRepo
    ) {
        this.adminRepo = adminRepo;
        this.userRepo = userRepo;
        this.postRepo = postRepo;
        this.reportRepo = reportRepo;
    }

    public List<User> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage = userRepo.findAll(pageable);
        return usersPage.toList();
    }

    public List<Post> getAllPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postsPage = postRepo.findAll(pageable);
        return postsPage.toList();
    }

    public List<Report> getAllReports(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Report> reportsPage = reportRepo.findByStatus(status, pageable);
        return reportsPage.toList();
    }

    
}
