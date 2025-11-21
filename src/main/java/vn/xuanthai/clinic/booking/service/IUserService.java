package vn.xuanthai.clinic.booking.service;

import vn.xuanthai.clinic.booking.dto.request.CreateUserRequest;
import vn.xuanthai.clinic.booking.dto.response.UserResponse;
import vn.xuanthai.clinic.booking.entity.User;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    User createUserByAdmin(CreateUserRequest request);
    Optional<User> findByEmail(String email);
    List<UserResponse> getAllUsers();
}
