package vn.xuanthai.clinic.booking.dto.request;

import lombok.Data;

@Data
public class TokenRefreshRequest {
    private String refreshToken;
}
