package vn.xuanthai.clinic.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.xuanthai.clinic.booking.dto.request.CreateUserRequest;
import vn.xuanthai.clinic.booking.entity.PasswordHistory;
import vn.xuanthai.clinic.booking.entity.Role;
import vn.xuanthai.clinic.booking.entity.User;
import vn.xuanthai.clinic.booking.exception.BadRequestException;
import vn.xuanthai.clinic.booking.repository.PasswordHistoryRepository;
import vn.xuanthai.clinic.booking.repository.RoleRepository;
import vn.xuanthai.clinic.booking.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordHistoryRepository passwordHistoryRepository;

    @Override
    public User createUserByAdmin(CreateUserRequest request) {
        // 1. Kiểm tra email đã tồn tại chưa
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email đã tồn tại.");
        }

        // 2. Tìm các đối tượng Role từ danh sách tên role gửi lên
        Set<Role> foundRoles = request.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new BadRequestException("Không tìm thấy vai trò: " + roleName)))
                .collect(Collectors.toSet());

        // 3. Tạo đối tượng User mới
        User newUser = new User();
        newUser.setFullName(request.getFullName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setPhoneNumber(request.getPhoneNumber());
        newUser.setAddress(request.getAddress());
        newUser.setActive(true);

        // 4. Gán các vai trò đã tìm thấy
        newUser.setRoles(foundRoles);

        // 5. Lưu vào CSDL
        return userRepository.save(newUser);
    }

    public void changeUserPassword(User user, String newPassword) {
        int historyLimit = 5; // Số lượng mật khẩu gần nhất cần giữ lại

        // 1. Lấy X mật khẩu cũ gần nhất (ví dụ: 5)
        Pageable pageable = PageRequest.of(0, historyLimit, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<PasswordHistory> recentPasswords = passwordHistoryRepository.findByUser(user, pageable);

        // 2. Kiểm tra xem mật khẩu mới có trùng với mật khẩu cũ không
        for (PasswordHistory oldPassword : recentPasswords) {
            if (passwordEncoder.matches(newPassword, oldPassword.getHashedPassword())) {
                throw new BadRequestException("Mật khẩu mới không được trùng với " + historyLimit + " mật khẩu gần nhất.");
            }
        }

        // 3. Nếu không trùng, băm mật khẩu mới
        String newHashedPassword = passwordEncoder.encode(newPassword);

        // 4. Cập nhật mật khẩu trong bảng users
        user.setPassword(newHashedPassword);
        userRepository.save(user);

        // 5. Lưu mật khẩu mới (đã băm) vào bảng password_history
        PasswordHistory historyEntry = new PasswordHistory(user, newHashedPassword);
        passwordHistoryRepository.save(historyEntry);

        // --- BƯỚC 6: XÓA CÁC BẢN GHI LỊCH SỬ CŨ HƠN X ---
        // Lấy tất cả ID của lịch sử mật khẩu của user này, sắp xếp theo thời gian tạo TĂNG DẦN
        List<Long> allHistoryIds = passwordHistoryRepository.findIdsByUserOrderByCreatedAtAsc(user);

        // Nếu số lượng lịch sử vượt quá giới hạn
        if (allHistoryIds.size() > historyLimit) {
            // Xác định số lượng cần xóa (ví dụ: có 7 cái, giữ lại 5 -> xóa 2 cái cũ nhất)
            int numberToDelete = allHistoryIds.size() - historyLimit;
            // Lấy ra danh sách ID của các bản ghi cũ nhất cần xóa
            List<Long> idsToDelete = allHistoryIds.subList(0, numberToDelete);

            // Xóa các bản ghi đó
            if (!idsToDelete.isEmpty()) {
                passwordHistoryRepository.deleteAllByIdInBatch(idsToDelete); // Dùng batch delete cho hiệu quả
            }
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
