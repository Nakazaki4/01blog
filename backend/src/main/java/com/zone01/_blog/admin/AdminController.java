package com.zone01._blog.admin;

import java.util.List;

import com.zone01._blog.report.ReportStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.zone01._blog.post.Post;
import com.zone01._blog.report.Report;
import com.zone01._blog.user.User;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
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
    public ResponseEntity<List<Report>> listAllReports(@AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0") int page,
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
                !status.equals(ReportStatus.DISMISSED) &&
                !status.equals(ReportStatus.REVIEWED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid reports status");
        }

        return ResponseEntity.ok(adminService.getAllReports(page, size, status));
    }

    @PostMapping("/users/{id}/ban")
    public ResponseEntity<Void> banUser(@PathVariable Long id){
        
    }
}
