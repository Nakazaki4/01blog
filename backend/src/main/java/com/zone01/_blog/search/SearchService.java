package com.zone01._blog.search;

import com.zone01._blog.search.dto.SearchUser;
import com.zone01._blog.user.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SearchService {

    private final SearchRepository searchRepository;

    public SearchService(SearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    public List<SearchUser> search(String username) {
        if (username.length() > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "search keyword is too long");
        }
        if (username.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no keyword");
        }
        List<User> users = searchRepository.findByKeyword(username, PageRequest.of(0, 20));
        return users.stream().map(this::toSearchUser).toList();
    }

    private SearchUser toSearchUser(User user) {
        return new SearchUser(user.getId(), user.getUsername(), user.getAvatarUrl());
    }
}
