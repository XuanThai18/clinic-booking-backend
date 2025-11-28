package vn.xuanthai.clinic.booking.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.xuanthai.clinic.booking.dto.request.CreateUserRequest;
import vn.xuanthai.clinic.booking.dto.request.UserUpdateRequest;
import vn.xuanthai.clinic.booking.dto.response.UserResponse;
import vn.xuanthai.clinic.booking.dto.response.UserResponsePage;
import vn.xuanthai.clinic.booking.entity.PasswordHistory;
import vn.xuanthai.clinic.booking.entity.Permission;
import vn.xuanthai.clinic.booking.entity.Role;
import vn.xuanthai.clinic.booking.entity.User;
import vn.xuanthai.clinic.booking.exception.BadRequestException;
import vn.xuanthai.clinic.booking.exception.ResourceNotFoundException;
import vn.xuanthai.clinic.booking.repository.PasswordHistoryRepository;
import vn.xuanthai.clinic.booking.repository.PermissionRepository;
import vn.xuanthai.clinic.booking.repository.RoleRepository;
import vn.xuanthai.clinic.booking.repository.UserRepository;
import vn.xuanthai.clinic.booking.service.IUserService;

import java.util.ArrayList;
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
    private final PermissionRepository permissionRepository;

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
        newUser.setGender(request.getGender());     // Lưu giới tính
        newUser.setBirthday(request.getBirthday()); // Lưu ngày sinh
        newUser.setActive(true);

        // 4. Gán các vai trò đã tìm thấy
        newUser.setRoles(foundRoles);

        //
        if (request.getExtraPermissions() != null && !request.getExtraPermissions().isEmpty()) {
            Set<Permission> permissions = request.getExtraPermissions().stream()
                    .map(permName -> permissionRepository.findByName(permName)
                            .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permName)))
                    .collect(Collectors.toSet());

            newUser.setExtraPermissions(permissions);
        }

        // 5. Lưu vào CSDL
        return userRepository.save(newUser);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToUserResponse(user);
    }

    @Override
    public UserResponse updateMyProfile(UserUpdateRequest request) {
        // 1. Lấy user đang đăng nhập từ SecurityContext
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Cập nhật thông tin (chỉ cập nhật nếu client có gửi giá trị lên)
        if (request.getFullName() != null) currentUser.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null) currentUser.setPhoneNumber(request.getPhoneNumber());
        if (request.getAddress() != null) currentUser.setAddress(request.getAddress());
        if (request.getGender() != null) currentUser.setGender(request.getGender());
        if (request.getBirthday() != null) currentUser.setBirthday(request.getBirthday());

        // 3. Lưu và trả về DTO
        User updatedUser = userRepository.save(currentUser);
        return mapToUserResponse(updatedUser);
    }

    @Override
    public UserResponse updateUser(Long id, CreateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFullName(request.getFullName());
        // Không cho đổi email vì là định danh
        // user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setGender(request.getGender());
        user.setBirthday(request.getBirthday());

        if (request.getIsActive() != null) {
            user.setActive(request.getIsActive());
        } else {
            user.setActive(true); // Mặc định active
        }

        // Cập nhật roles nếu có thay đổi
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> newRoles = request.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new BadRequestException("Role not found")))
                    .collect(Collectors.toSet());
            user.setRoles(newRoles);
        }

        // 2. --- UPDATE EXTRA PERMISSIONS (MỚI) ---
        if (request.getExtraPermissions() != null) {
            Set<Permission> permissions = request.getExtraPermissions().stream()
                    .map(permName -> permissionRepository.findByName(permName)
                            .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permName)))
                    .collect(Collectors.toSet());

            user.setExtraPermissions(permissions);
        }

        // Cập nhật mật khẩu nếu có (tùy chọn)
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return mapToUserResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(id);
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

    @Override
    public List<UserResponse> getAllUsers() {
        // 1. Lấy tất cả user từ DB
        List<User> users = userRepository.findAll();

        // 2. Chuyển đổi (Map) từng User entity sang UserResponse DTO
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponsePage getAllUsersWithSearch(String keyword, String roleName, Pageable pageable) {

        // Gọi Repository trực tiếp - Đơn giản và hiệu quả hơn
        Page<User> pageResult = userRepository.searchUsers(keyword, roleName, pageable);

        // Phần map và trả về giữ nguyên
        List<UserResponse> content = pageResult.getContent().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());

        return UserResponsePage.builder()
                .content(content)
                .pageNo(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    // Trong UserServiceImpl.java

    @Override
    @Transactional
    public void grantPermissionToUser(Long userId, String permissionName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permissionName));

        // Thêm quyền vào danh sách riêng
        user.getExtraPermissions().add(permission);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void revokePermissionFromUser(Long userId, String permissionName) {
        User user = userRepository.findById(userId).orElseThrow();

        // Xóa quyền khỏi danh sách riêng (dùng removeIf)
        user.getExtraPermissions().removeIf(p -> p.getName().equals(permissionName));

        userRepository.save(user);
    }

    // --- HÀM TRỢ GIÚP (Copy từ Controller vào đây để dùng chung) ---
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
        // Lấy danh sách tên các role
        dto.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));
        dto.setExtraPermissions(user.getExtraPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet()));
        return dto;
    }
}
