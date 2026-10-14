package vn.xuanthai.clinic.booking.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.xuanthai.clinic.booking.entity.RefreshToken;
import vn.xuanthai.clinic.booking.repository.RefreshTokenRepository;
import vn.xuanthai.clinic.booking.repository.UserRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${application.security.jwt.refresh-token.expiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshToken createRefreshToken(String username) {
        // 1. Tìm user
        var user = userRepository.findByEmail(username).orElseThrow();

        // 2. Xóa token cũ của user này (nếu có) để tránh rác DB
        // (Tùy logic, có thể cho phép đa thiết bị thì không xóa)
        // refreshTokenRepository.deleteByUser(user);

        // 3. Tạo token mới
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .token(UUID.randomUUID().toString()) // Tạo chuỗi ngẫu nhiên
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token đã hết hạn. Vui lòng đăng nhập lại!");
        }
        return token;
    }

    // Tìm token trong DB
    public RefreshToken findByToken(String token){
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại!"));
    }
}
