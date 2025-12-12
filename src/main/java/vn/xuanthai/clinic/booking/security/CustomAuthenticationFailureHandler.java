package vn.xuanthai.clinic.booking.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import vn.xuanthai.clinic.booking.entity.User;
import vn.xuanthai.clinic.booking.repository.UserRepository;
import vn.xuanthai.clinic.booking.service.impl.LoginAttemptService; // Sẽ tạo service này

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final LoginAttemptService loginAttemptService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper; // Inject ObjectMapper để tạo JSON

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException originalException) throws IOException, ServletException { // Đổi tên tham số gốc

        String email = request.getParameter("username"); // Lấy email
        AuthenticationException exceptionToUse = originalException; // Mặc định dùng lỗi gốc
        boolean accountLocked = false; // Cờ để biết tài khoản có bị khóa không

        if (email != null) {
            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                loginAttemptService.loginFailed(email); // Ghi nhận thất bại

                if (loginAttemptService.isBlocked(email)) {
                    accountLocked = true; // Đánh dấu là đã khóa
                    // Khóa tài khoản nếu cần
                    if (user.getLockoutEndTime() == null || user.getLockoutEndTime().isBefore(LocalDateTime.now())) {
                        user.setLockoutEndTime(LocalDateTime.now().plusMinutes(15));
                        userRepository.save(user);
                    }
                    // Chuẩn bị exception cụ thể cho trường hợp bị khóa
                    exceptionToUse = new LockedException("Tài khoản đã bị khóa tạm thời do nhập sai mật khẩu quá nhiều lần.");
                }
            }
        }

        // --- Trả về lỗi JSON thay vì redirect ---
        response.setStatus(HttpStatus.UNAUTHORIZED.value()); // Luôn trả về 401 cho lỗi đăng nhập
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", LocalDateTime.now().toString());
        data.put("status", HttpStatus.UNAUTHORIZED.value());
        data.put("error", "Unauthorized");
        // Lấy message từ exception cuối cùng
        data.put("message", exceptionToUse.getMessage());
        data.put("path", request.getRequestURI());
        // Thêm cờ báo tài khoản bị khóa (tùy chọn)
        if (accountLocked) {
            data.put("accountLocked", true);
        }

        OutputStream out = response.getOutputStream();
        objectMapper.writeValue(out, data); // Dùng ObjectMapper để ghi JSON
        out.flush();
    }
}