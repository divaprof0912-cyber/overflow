package com.overflow.user;

import com.overflow.user.dto.CreatePartnerRequest;
import com.overflow.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/partners")
public class AdminPartnerController {
    private final UserService users;

    public AdminPartnerController(UserService users) {
        this.users = users;
    }

    @GetMapping
    public List<UserResponse> list() { return users.listPartners(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreatePartnerRequest request) {
        return users.createPartner(request);
    }
}
