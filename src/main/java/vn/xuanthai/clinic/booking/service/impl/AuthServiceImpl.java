package vn.xuanthai.clinic.booking.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.xuanthai.clinic.booking.dto.request.AuthRequest;
import vn.xuanthai.clinic.booking.dto.request.RegisterRequest;
import vn.xuanthai.clinic.booking.dto.response.AuthResponse;
import vn.xuanthai.clinic.booking.dto.response.UserResponse;
import vn.xuanthai.clinic.booking.entity.Role;
import vn.xuanthai.clinic.booking.entity.User;
import vn.xuanthai.clinic.booking.exception.BadRequestException;
import vn.xuanthai.clinic.booking.repository.RoleRepository;
import vn.xuanthai.clinic.booking.repository.UserRepository;
import vn.xuanthai.clinic.booking.security.JwtService;
import vn.xuanthai.clinic.booking.security.UserDetailsImpl;
import vn.xuanthai.clinic.booking.service.IAuthService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final CaptchaService captchaService;

    @Override
    public User register(RegisterRequest request) {
        // KIỂM TRA CAPTCHA
        captchaService.validateCaptcha(request.getCaptchaResponse());

        // 1. Kiểm tra xem email đã tồn tại chưa
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email đã tồn tại. Vui lòng sử dụng email khác.");
        }

        // 2. Tạo đối tượng User mới
        User newUser = new User();
        newUser.setFullName(request.getFullName());
        newUser.setEmail(request.getEmail());
        newUser.setPhoneNumber(request.getPhoneNumber());
        newUser.setAddress(request.getAddress());
        newUser.setGender(request.getGender());
        newUser.setBirthday(request.getBirthday());

        // 3. Băm mật khẩu
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        // 4. Gán vai trò mặc định là "PATIENT"
        Role patientRole = roleRepository.findByName("ROLE_PATIENT")
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy vai trò PATIENT."));
        newUser.setRoles(Set.of(patientRole));

        // 5. Lưu vào CSDL
        return userRepository.save(newUser);
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        // KIỂM TRA CAPTCHA
        captchaService.validateCaptcha(request.getCaptchaResponse());

        // KIỂM TRA TRẠNG THÁI KHÓA TRƯỚC KHI XÁC THỰC
        if (loginAttemptService.isBlocked(request.getEmail())) {
            // Kiểm tra xem khóa trong CSDL (nếu có) đã hết hạn chưa
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getLockoutEndTime() != null && user.getLockoutEndTime().isAfter(LocalDateTime.now())) {
                    // Nếu vẫn còn trong thời gian bị khóa, ném lỗi
                    throw new BadRequestException("Tài khoản đã bị khóa tạm thời. Vui lòng thử lại sau 15 phút.");
                } else if (user.getLockoutEndTime() != null) {
                    // Nếu thời gian khóa đã hết, ta reset lại bộ đếm
                    loginAttemptService.loginSucceeded(request.getEmail()); // Xóa cache
                    user.setFailedLoginAttempts(0); // Đặt lại CSDL
                    user.setLockoutEndTime(null);
                    userRepository.save(user);
                }
            }
        }

        // TIẾN HÀNH XÁC THỰC
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            //  (Thất bại): GỌI LOGIC XỬ LÝ LỖI ---
            loginAttemptService.loginFailed(request.getEmail()); // Ghi nhận lỗi

            // Kiểm tra xem lần thất bại này có gây ra khóa không
            if (loginAttemptService.isBlocked(request.getEmail())) {
                userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
                    user.setLockoutEndTime(LocalDateTime.now().plusMinutes(15));
                    user.setFailedLoginAttempts(LoginAttemptService.MAX_ATTEMPT); // Gán số lần sai tối đa
                    userRepository.save(user);
                });
                throw new BadRequestException("Tài khoản đã bị khóa tạm thời do nhập sai mật khẩu quá nhiều lần.");
            }

            // Nếu không, chỉ là sai mật khẩu
            throw new BadRequestException("Email hoặc mật khẩu không hợp lệ.");
        }

        // GỌI LOGIC XỬ LÝ THÀNH CÔNG
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user sau khi xác thực"));

        // Reset bộ đếm (Logic từ CustomAuthenticationSuccessHandler)
        loginAttemptService.loginSucceeded(user.getEmail());
        if (user.getFailedLoginAttempts() > 0 || user.getLockoutEndTime() != null) {
            user.setFailedLoginAttempts(0);
            user.setLockoutEndTime(null);
            userRepository.save(user);
        }

        // 3. Tạo JWT token
        var jwtToken = jwtService.generateToken(userDetails);

        // 4. Ánh xạ User sang UserResponse (DTO)
        UserResponse userDto = mapToUserResponse(user);

        // 5. Trả về cả token và userDto
        return AuthResponse.builder()
                .accessToken(jwtToken)
                .user(userDto) // <-- THÊM VÀO RESPONSE
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        return dto;
    }
}
