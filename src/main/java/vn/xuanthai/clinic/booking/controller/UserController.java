package vn.xuanthai.clinic.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.xuanthai.clinic.booking.dto.request.UserUpdateRequest;
import vn.xuanthai.clinic.booking.dto.response.UserResponse;
import vn.xuanthai.clinic.booking.service.IUserService;

@RestController
@RequestMapping("/api/users") // Đảm bảo class có dòng này
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @PutMapping("/profile/me")
    @PreAuthorize("isAuthenticated()") // Ai đăng nhập rồi cũng dùng được
    public ResponseEntity<UserResponse> updateMyProfile(@RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateMyProfile(request));
    }
}