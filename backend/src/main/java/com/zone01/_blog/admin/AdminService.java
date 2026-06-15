package com.zone01._blog.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.zone01._blog.admin.dto.AdminPostDto;
import com.zone01._blog.admin.dto.AdminReportDto;
import com.zone01._blog.admin.dto.AdminUserDto;
import com.zone01._blog.post.Post;
import com.zone01._blog.post.PostRepository;
import com.zone01._blog.post.dto.UserPost;
import com.zone01._blog.report.Report;
import com.zone01._blog.report.ReportRepository;
import com.zone01._blog.report.ReportStatus;
import com.zone01._blog.user.User;
import com.zone01._blog.user.UserRepository;

import jakarta.transaction.Transactional;

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

    public record AdminStats(long totalUsers, long totalPosts, long totalReports, long totalPendingReports) {}

    public AdminStats getStats() {
        return new AdminStats(
                userRepo.count(),
                postRepo.count(),
                reportRepo.count(),
                reportRepo.countByStatus(ReportStatus.PENDING)
        );
    }

    @Transactional
    public List<AdminUserDto> getAllUsers(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = search == null || search.isBlank()
                ? userRepo.findAll(pageable)
                : userRepo.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        search.trim(),
                        search.trim(),
                        pageable
                );
        return users.map(this::toAdminUserDto).toList();
    }

    @Transactional
    public List<AdminPostDto> getAllPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepo.findAllWithAdminCounts(pageable).map(this::toAdminPostDto).toList();
    }

    @Transactional
    public List<AdminReportDto> getAllReports(int page, int size, ReportStatus status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Report> reports = status == null
                ? reportRepo.findAll(pageable)
                : reportRepo.findByStatus(status, pageable);
        return reports.map(this::toAdminReportDto).toList();
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
    public AdminReportDto updateReportStatus(Long reportId, ReportStatus newStatus) {
        Report report = reportRepo.findById(reportId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "report doesn't exist")
                );

        reportRepo.updateStatus(reportId, newStatus);
        report.setStatus(newStatus);
        return toAdminReportDto(report);
    }

    public void deletePost(Long postId) {
        try {
            postRepo.deleteById(postId);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown error occurred");
        }
    }

    private AdminUserDto toAdminUserDto(User user) {
        return new AdminUserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isBanned(),
                postRepo.countByUserId(user.getId()),
                reportRepo.countByReportedUserId(user.getId())
        );
    }

    private AdminPostDto toAdminPostDto(Object[] row) {
        Post post = (Post) row[0];
        long likeCount = ((Number) row[1]).longValue();
        long commentCount = ((Number) row[2]).longValue();
        long reportCount = ((Number) row[3]).longValue();
        User author = post.getUser();

        return new AdminPostDto(
                post.getId(),
                new UserPost(author.getId(), author.getUsername(), author.getAvatarUrl()),
                post.getDescription(),
                likeCount,
                commentCount,
                reportCount,
                post.isDeleted(),
                post.getCreatedAt()
        );
    }

    private AdminReportDto toAdminReportDto(Report report) {
        User reporter = report.getReporter();
        User reportedUser = report.getReportedUser();
        return new AdminReportDto(
                report.getId(),
                new UserPost(reporter.getId(), reporter.getUsername(), reporter.getAvatarUrl()),
                new UserPost(reportedUser.getId(), reportedUser.getUsername(), reportedUser.getAvatarUrl()),
                report.getReason(),
                report.getStatus(),
                report.getCreatedAt()
        );
    }
}
