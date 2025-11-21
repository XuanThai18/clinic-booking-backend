package vn.xuanthai.clinic.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.xuanthai.clinic.booking.dto.request.CreateUserRequest;
import vn.xuanthai.clinic.booking.dto.response.UserResponse;
import vn.xuanthai.clinic.booking.entity.Role;
import vn.xuanthai.clinic.booking.entity.User;
import vn.xuanthai.clinic.booking.service.IUserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IUserService userService;

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('USER_MANAGE_ROLES')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        // 1. Gọi service để tạo User Entity
        User createdUser = userService.createUserByAdmin(request);

        // 2. Ánh xạ (Map) từ User Entity sang UserResponse DTO để trả về
        UserResponse userResponseDto = mapToUserResponse(createdUser);

        // 3. Trả về DTO trong body của response
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDto);
    }

    @GetMapping("/users")
    // Chỉ Admin hoặc Super Admin mới được xem danh sách user
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Phương thức trợ giúp để thực hiện việc ánh xạ
    private UserResponse mapToUserResponse(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());
        // Lấy danh sách tên của các role
        dto.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        return dto;
    }
}