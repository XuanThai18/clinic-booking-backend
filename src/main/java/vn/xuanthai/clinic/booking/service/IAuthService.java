package vn.xuanthai.clinic.booking.service;

import vn.xuanthai.clinic.booking.dto.request.AuthRequest;
import vn.xuanthai.clinic.booking.dto.request.RegisterRequest;
import vn.xuanthai.clinic.booking.dto.response.AuthResponse;
import vn.xuanthai.clinic.booking.entity.User;

public interface IAuthService {
    User register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
}
