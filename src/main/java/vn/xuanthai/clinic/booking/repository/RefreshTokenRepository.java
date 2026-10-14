package vn.xuanthai.clinic.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import vn.xuanthai.clinic.booking.entity.RefreshToken;
import vn.xuanthai.clinic.booking.entity.User;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    int deleteByUser(User user); // Để xóa token cũ khi đăng xuất/đăng nhập lại
}
