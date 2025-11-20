package vn.xuanthai.clinic.booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder // <-- THÊM ANNOTATION NÀY VÀO
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private UserResponse user;
}