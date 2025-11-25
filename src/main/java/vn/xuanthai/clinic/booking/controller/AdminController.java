package vn.xuanthai.clinic.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.xuanthai.clinic.booking.dto.request.CreateUserRequest;
import vn.xuanthai.clinic.booking.dto.response.UserResponse;
import vn.xuanthai.clinic.booking.dto.response.UserResponsePage;
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
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        // 1. Gọi service để tạo User Entity
        User createdUser = userService.createUserByAdmin(request);

        // 2. Ánh xạ (Map) từ User Entity sang UserResponse DTO để trả về
        UserResponse userResponseDto = mapToUserResponse(createdUser);

        // 3. Trả về DTO trong body của response
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDto);
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyAuthority('USER_VIEW')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('USER_EDIT')") // Chỉ Super Admin mới sửa được user khác
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')") // Chỉ Super Admin mới xóa được user
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users") // Thay thế hàm getAllUsers cũ
    @PreAuthorize("hasAnyAuthority('USER_VIEW')")
    public ResponseEntity<UserResponsePage> getAllUsers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page, // Trang mặc định là 0 (trang 1)
            @RequestParam(defaultValue = "10") int size, // Mặc định 10 người/trang
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        // Tạo đối tượng Pageable
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(userService.getAllUsersWithSearch(keyword, role, pageable));
    }

    // Phương thức trợ giúp để thực hiện việc ánh xạ
    private UserResponse mapToUserResponse(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setGender(user.getGender());
        dto.setBirthday(user.getBirthday());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());
        // Lấy danh sách tên của các role
        dto.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        return dto;
    }
}