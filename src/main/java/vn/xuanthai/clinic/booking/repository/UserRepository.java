package vn.xuanthai.clinic.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.xuanthai.clinic.booking.entity.User; // Đảm bảo import đúng
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // THÊM DÒNG NÀY VÀO
    Optional<User> findByEmail(String email);
}
