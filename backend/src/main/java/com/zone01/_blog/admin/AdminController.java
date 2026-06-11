package com.zone01._blog.admin;

import com.zone01._blog.post.Post;
import com.zone01._blog.report.Report;
import com.zone01._blog.report.ReportStatus;
import com.zone01._blog.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    private static Long parseId(String principal) {
        if (principal == null || "anonymousUser".equals(principal)) return -1L;
        return Long.parseLong(principal);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> listAllUsers(@AuthenticationPrincipal String userId,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page can't be negative");
        } else if (size > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size can't be greater"
                    + " than 20");
        }
        return ResponseEntity.ok(adminService.getAllUsers(page, size));
    }

    @GetMapping("/posts")
    public ResponseEntity<List<Post>> listAllPosts(@AuthenticationPrincipal String userId,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page can't be negative");
        } else if (size > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size can't be greater"
                    + " than 20");
        }
        return ResponseEntity.ok(adminService.getAllPosts(page, size));
    }

    @GetMapping("/reports")
    public ResponseEntity<List<Report>> listAllReports(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size,
                                                       @RequestParam(defaultValue = "PENDING") String status
    ) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page can't be negative");
        } else if (size > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size can't be greater"
                    + " than 20");
        }

        if (!status.equals(ReportStatus.PENDING) &&
                !status.equals(ReportStatus.REVIEWED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid reports status");
        }

        return ResponseEntity.ok(adminService.getAllReports(page, size, status));
    }

    @PostMapping("/users/{id}/ban")
    public ResponseEntity<Void> banUser(@AuthenticationPrincipal String adminId, @PathVariable Long id) {
        if (parseId(adminId).equals(id)) {
            return ResponseEntity.badRequest().build();
        }
        adminService.switchBanState(id, true);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{id}/unban")
    public ResponseEntity<Void> unbanUser(@AuthenticationPrincipal String adminId, @PathVariable Long id) {
        if (parseId(adminId).equals(id)) {
            return ResponseEntity.badRequest().build();
        }
        adminService.switchBanState(id, false);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal String adminId, @PathVariable Long id) {
        if (parseId(adminId).equals(id)) {
            return ResponseEntity.badRequest().build();
        }
        adminService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@AuthenticationPrincipal String adminId, @PathVariable Long id) {
        adminService.deletePost(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/reports/{id}")
    public ResponseEntity<ReportStatus> updateReportStatus(Long reportId) {
        return ResponseEntity.ok(adminService.updateReportStatus(reportId));
    }
}
