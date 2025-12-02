package vn.xuanthai.clinic.booking.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.xuanthai.clinic.booking.entity.User;
import vn.xuanthai.clinic.booking.exception.ResourceNotFoundException;
import vn.xuanthai.clinic.booking.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserContextService {

    private final UserRepository userRepository;

    /**
     * Lấy User Entity của người đang đăng nhập hiện tại
     */
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng đang đăng nhập."));
    }

    /**
     * Lấy Clinic ID của người đang đăng nhập (Trả về null nếu là Super Admin)
     */
    public Long getCurrentClinicId() {
        User user = getCurrentUser();
        return user.getClinicId(); // Giả sử em đã thêm getter getClinicId() trong Entity User
    }
}