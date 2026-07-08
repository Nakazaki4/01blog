package com.zone01._blog.report;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.zone01._blog.post.Post;
import com.zone01._blog.post.PostRepository;
import com.zone01._blog.report.dto.ReportResponse;
import com.zone01._blog.user.User;
import com.zone01._blog.user.UserRepository;

@Service
public class ReportService {

    private final UserRepository userRepo;
    private final PostRepository postRepo;
    private final ReportRepository reportRepo;

    public ReportService(UserRepository userRepo, PostRepository postRepo,
            ReportRepository reportRepo) {
        this.userRepo = userRepo;
        this.postRepo = postRepo;
        this.reportRepo = reportRepo;
    }

    public ReportResponse validateAndSaveUserReport(Long reporterId, Long reportedUserId, String reason) {
        if (reason == null || reason.length() < 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Short reason text");
        }
        if (reason.length() > 300) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Long reason text");
        }
        if (reporterId.equals(reportedUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "can't report yourself");
        }

        User reportedUser = userRepo.findById(reportedUserId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        User reporter = userRepo.findById(reporterId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

        Report saved;
        try {
            Report report = createReport(reportedUser, reporter, reason, ReportType.USER, null);
            saved = reportRepo.save(report);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already reported this user");
        }

        return new ReportResponse(saved.getId(), saved.getReportedUser().getId(), saved.getReason(),
                saved.getStatus(), saved.getCreatedAt());
    }

    public ReportResponse ValidateAndSaveReport(Long userId, Long postId, String reason) {
        if (reason.length() > 300) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Long reason text");
        }

        if (reason.length() < 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Short reason text");
        }

        Post post = postRepo.findById(postId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post doesn't exist"));

        User postOwner = post.getUser();
        User reporter = userRepo.findById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
        );

        if (postOwner.equals(reporter)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "can't report your own post");
        }

        Report saved;
        try {
            Report report = createReport(postOwner, reporter, reason, ReportType.POST, post);
            saved = reportRepo.save(report);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You already reported this post"
            );
        }

        return new ReportResponse(saved.getId(), saved.getReportedUser().getId(), saved.getReason(),
                saved.getStatus(), saved.getCreatedAt());
    }

    private Report createReport(User reportedUser, User reporter, String reason,
            ReportType type, Post reportedPost) {
        Report report = new Report();
        report.setReportedUser(reportedUser);
        report.setReporter(reporter);
        report.setStatus(ReportStatus.PENDING);
        report.setReason(reason);
        report.setType(type);
        report.setReportedPost(reportedPost);
        return report;
    }
}
