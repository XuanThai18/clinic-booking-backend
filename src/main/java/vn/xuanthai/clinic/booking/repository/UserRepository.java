package vn.xuanthai.clinic.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.xuanthai.clinic.booking.entity.User; // Đảm bảo import đúng
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // THÊM DÒNG NÀY VÀO
    Optional<User> findByEmail(String email);

    // --- HÀM TÌM KIẾM MẠNH MẼ ---
    @Query("""
    SELECT u FROM User u JOIN u.roles r
    WHERE (:keyword = ''
       OR LOWER(FUNCTION('unaccent', u.fullName)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :keyword, '%')))
       OR LOWER(FUNCTION('unaccent', u.email)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :keyword, '%')))
    )
    AND (:roleName = '' OR r.name = :roleName)
    """)
    Page<User> searchUsers(@Param("keyword") String keyword,
                           @Param("roleName") String roleName,
                           Pageable pageable);

    Optional<User> findByResetPasswordToken(String token);
}
