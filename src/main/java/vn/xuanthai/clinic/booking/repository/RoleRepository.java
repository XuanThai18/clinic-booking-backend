package vn.xuanthai.clinic.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.xuanthai.clinic.booking.entity.Role;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Dùng cho việc "gieo dữ liệu" (DataSeeder) để tránh tạo trùng lặp
    Optional<Role> findByName(String name);
}
