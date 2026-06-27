package com.zone01._blog.admin;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.zone01._blog.admin.dto.AdminPostDto;
import com.zone01._blog.admin.dto.AdminReportDto;
import com.zone01._blog.admin.dto.AdminUserDto;
import com.zone01._blog.report.ReportStatus;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    public record ReportStatusUpdate(ReportStatus status) {
    }

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    private static Long parseId(String principal) {
        if (principal == null || "anonymousUser".equals(principal)) return -1L;
        return Long.parseLong(principal);
    }

    private static ReportStatus parseReportStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return ReportStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid reports status");
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminService.AdminStats> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> listAllUsers(@AuthenticationPrincipal String userId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size,
                                                           @RequestParam(defaultValue = "") String search) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page can't be negative");
        } else if (size > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size can't be greater than 20");
        }
        return ResponseEntity.ok(adminService.getAllUsers(page, size, search));
    }

    @GetMapping("/posts")
    public ResponseEntity<List<AdminPostDto>> listAllPosts(@AuthenticationPrincipal String userId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page can't be negative");
        } else if (size > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size can't be greater than 20");
        }
        return ResponseEntity.ok(adminService.getAllPosts(page, size));
    }

    @GetMapping("/reports")
    public ResponseEntity<List<AdminReportDto>> listAllReports(@RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "20") int size,
                                                               @RequestParam(required = false) String status
    ) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page can't be negative");
        } else if (size > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size can't be greater than 20");
        }
        return ResponseEntity.ok(adminService.getAllReports(page, size, parseReportStatus(status)));
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

    @PostMapping("/posts/{id}/hide")
    public ResponseEntity<Void> hidePost(@AuthenticationPrincipal String adminId, @PathVariable Long id) {
        adminService.switchHiddenState(id, true);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/posts/{id}/unhide")
    public ResponseEntity<Void> unhidePost(@AuthenticationPrincipal String adminId, @PathVariable Long id) {
        adminService.switchHiddenState(id, false);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/reports/{id}")
    public ResponseEntity<AdminReportDto> updateReportStatus(
            @PathVariable Long id,
            @RequestBody ReportStatusUpdate request
    ) {
        if (request == null || request.status() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");
        }
        return ResponseEntity.ok(adminService.updateReportStatus(id, request.status()));
    }
}
