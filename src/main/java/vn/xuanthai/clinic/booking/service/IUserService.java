package vn.xuanthai.clinic.booking.service;

import org.springframework.data.domain.Pageable;
import vn.xuanthai.clinic.booking.dto.request.CreateUserRequest;
import vn.xuanthai.clinic.booking.dto.request.UserUpdateRequest;
import vn.xuanthai.clinic.booking.dto.response.UserResponse;
import vn.xuanthai.clinic.booking.dto.response.UserResponsePage;
import vn.xuanthai.clinic.booking.entity.User;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    User createUserByAdmin(CreateUserRequest request);
    UserResponse updateMyProfile(UserUpdateRequest request);
    Optional<User> findByEmail(String email);
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, CreateUserRequest request);
    void deleteUser(Long id);
    // Hàm tìm kiếm nâng cao
    UserResponsePage getAllUsersWithSearch(String keyword, String roleName, Pageable pageable);
    void grantPermissionToUser(Long userId, String permissionName);
    void revokePermissionFromUser(Long userId, String permissionName);
}
