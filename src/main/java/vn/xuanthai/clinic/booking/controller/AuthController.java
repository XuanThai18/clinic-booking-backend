package vn.xuanthai.clinic.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import vn.xuanthai.clinic.booking.dto.request.*;
import vn.xuanthai.clinic.booking.dto.response.AuthResponse;
import vn.xuanthai.clinic.booking.dto.response.TokenRefreshResponse;
import vn.xuanthai.clinic.booking.dto.response.UserResponse;
import vn.xuanthai.clinic.booking.entity.RefreshToken;
import vn.xuanthai.clinic.booking.repository.RefreshTokenRepository;
import vn.xuanthai.clinic.booking.security.JwtService;
import vn.xuanthai.clinic.booking.security.UserDetailsImpl;
import vn.xuanthai.clinic.booking.service.IAuthService;
import vn.xuanthai.clinic.booking.service.IUserService;
import vn.xuanthai.clinic.booking.service.impl.RefreshTokenService;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth") // Đặt đường dẫn gốc cho các API xác thực
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;
    private final IUserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Đăng ký tài khoản thành công!");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // API 1: Gửi yêu cầu quên mật khẩu
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok("Vui lòng kiểm tra email để lấy lại mật khẩu.");
    }

    // API 2: Đặt lại mật khẩu
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return Optional.of(refreshTokenService.findByToken(requestRefreshToken))
                .map(refreshTokenService::verifyExpiration) // Kiểm tra hết hạn chưa
                .map(RefreshToken::getUser) // Lấy User từ token
                .map(user -> {
                    UserDetails userDetails = org.springframework.security.core.userdetails.User
                            .withUsername(user.getEmail()) // Lấy email làm username
                            .password(user.getPassword())  // Lấy password
                            .authorities("ROLE_USER")      // Set quyền (Hoặc lấy từ user.getRole())
                            .build();
                    // Tạo Access Token mới
                    String newAccessToken = jwtService.generateToken(userDetails);
                    // Trả về Access Token mới và Refresh Token cũ (hoặc tạo mới tùy bạn)
                    return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, requestRefreshToken));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token không hợp lệ!"));
    }
}