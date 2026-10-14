package vn.xuanthai.clinic.booking.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRefreshResponse {
    private String accessToken;
    private String refreshToken;
}
