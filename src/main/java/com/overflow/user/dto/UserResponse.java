package com.overflow.user.dto;

import com.overflow.user.AppUser;
import com.overflow.user.UserRole;

public record UserResponse(Long id, String name, String email, String phone, UserRole role) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole());
    }
}
