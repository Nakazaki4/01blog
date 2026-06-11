package com.zone01._blog.admin;

import com.zone01._blog.post.Post;
import com.zone01._blog.post.PostRepository;
import com.zone01._blog.report.Report;
import com.zone01._blog.report.ReportRepository;
import com.zone01._blog.report.ReportStatus;
import com.zone01._blog.user.User;
import com.zone01._blog.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepo;
    private final PostRepository postRepo;
    private final ReportRepository reportRepo;

    public AdminService(UserRepository userRepo,
                        PostRepository postRepo, ReportRepository reportRepo
    ) {
        this.userRepo = userRepo;
        this.postRepo = postRepo;
        this.reportRepo = reportRepo;
    }

    @Transactional
    public List<User> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage = userRepo.findAll(pageable);
        return usersPage.toList();
    }

    @Transactional
    public List<Post> getAllPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postsPage = postRepo.findAll(pageable);
        return postsPage.toList();
    }

    @Transactional
    public List<Report> getAllReports(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Report> reportsPage = reportRepo.findByStatus(status, pageable);
        return reportsPage.toList();
    }

    @Transactional
    public void switchBanState(Long userId, boolean flag) {
        boolean exists = userRepo.existsById(userId);
        if (!exists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "user doesn't exist");
        }
        userRepo.switchBannedToTrue(userId, flag);
    }

    @Transactional
    public void deleteUser(Long userId) {
        try {
            userRepo.deleteById(userId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "unknown error occurred");
        }
    }

    @Transactional
    public ReportStatus updateReportStatus(Long reportId) {
        Report report = reportRepo.findById(reportId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "report doesn't exist")
                );

        ReportStatus newStatus;

        switch (report.getStatus()) {
            case PENDING -> newStatus = ReportStatus.REVIEWED;
            case REVIEWED -> newStatus = ReportStatus.PENDING;
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid status"
            );
        }
        reportRepo.updateStatus(reportId, newStatus.name());
        return newStatus;
    }
}
