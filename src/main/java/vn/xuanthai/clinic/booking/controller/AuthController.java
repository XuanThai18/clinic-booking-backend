package vn.xuanthai.clinic.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.xuanthai.clinic.booking.dto.request.AuthRequest;
import vn.xuanthai.clinic.booking.dto.request.RegisterRequest;
import vn.xuanthai.clinic.booking.dto.request.UserUpdateRequest;
import vn.xuanthai.clinic.booking.dto.response.AuthResponse;
import vn.xuanthai.clinic.booking.dto.response.UserResponse;
import vn.xuanthai.clinic.booking.service.IAuthService;
import vn.xuanthai.clinic.booking.service.IUserService;

@RestController
@RequestMapping("/api/auth") // Đặt đường dẫn gốc cho các API xác thực
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;
    private final IUserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Đăng ký tài khoản thành công!");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PutMapping("/me") // Endpoint: PUT /api/users/me
    @PreAuthorize("isAuthenticated()") // Ai đăng nhập rồi cũng dùng được
    public ResponseEntity<UserResponse> updateMyProfile(@RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateMyProfile(request));
    }
}