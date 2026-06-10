package com.zone01._blog.report;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public ReportResponse ValidateAndSaveReport(Long userId, Long postId, String reason) {
        if (reason.length() > 300) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Long reason text");
        }

        if (reason.length() < 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Short reason text");
        }

        boolean exists = postRepo.existsById(postId);
        if (!exists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post doesn't exist");
        }

        User postOwner = userRepo.findByPostId(postId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

        User reporter = userRepo.findById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
        );

        if (postOwner.equals(reporter)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "can't report your own post");
        }
        
        Report saved;
        try {
            Report report = createReport(postOwner, reporter, reason);
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

    private Report createReport(User postOwner, User reporter, String reason) {
        Report report = new Report();
        report.setReportedUser(postOwner);
        report.setReporter(reporter);
        report.setStatus(ReportStatus.PENDING);
        report.setReason(reason);
        return report;
    }
}
