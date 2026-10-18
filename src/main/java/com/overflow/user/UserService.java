package com.overflow.user;

import com.overflow.common.ApiException;
import com.overflow.user.dto.CreatePartnerRequest;
import com.overflow.user.dto.CustomerRegistrationRequest;
import com.overflow.user.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.List;

@Service
public class UserService {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse registerCustomer(CustomerRegistrationRequest request) {
        return UserResponse.from(create(request.name(), request.email(), request.password(), request.phone(), UserRole.CUSTOMER));
    }

    @Transactional
    public UserResponse createPartner(CreatePartnerRequest request) {
        return UserResponse.from(create(request.name(), request.email(), request.password(), request.phone(), UserRole.PARTNER));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listPartners() {
        return users.findAllByRoleAndActiveTrueOrderByNameAsc(UserRole.PARTNER)
                .stream().map(UserResponse::from).toList();
    }

    @Transactional
    public boolean createBootstrapAdmin(String name, String email, String rawPassword) {
        if (name == null || name.isBlank() || email == null || email.isBlank()
                || rawPassword == null || rawPassword.isBlank()) {
            return false;
        }
        String normalizedEmail = normalize(email);
        if (users.existsByEmailIgnoreCase(normalizedEmail)) {
            return false;
        }
        users.save(new AppUser(name.trim(), normalizedEmail, passwordEncoder.encode(rawPassword), null, UserRole.ADMIN));
        return true;
    }

    @Transactional(readOnly = true)
    public AppUser requirePartner(Long id) {
        AppUser user = users.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Delivery partner not found"));
        if (user.getRole() != UserRole.PARTNER || !user.isActive()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Selected account is not an active delivery partner");
        }
        return user;
    }

    private AppUser create(String name, String email, String rawPassword, String phone, UserRole role) {
        String normalizedEmail = normalize(email);
        if (users.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        AppUser user = new AppUser(name.trim(), normalizedEmail, passwordEncoder.encode(rawPassword),
                phone == null || phone.isBlank() ? null : phone.trim(), role);
        return users.save(user);
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
