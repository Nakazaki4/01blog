package com.zone01._blog.search;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.zone01._blog.search.dto.SearchUser;
import com.zone01._blog.user.User;

@Controller
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<List<SearchUser>> search(@RequestParam String keyword) {
        List<SearchUser> users = searchService.search(keyword);
        return ResponseEntity.ok(users);
    }
}
