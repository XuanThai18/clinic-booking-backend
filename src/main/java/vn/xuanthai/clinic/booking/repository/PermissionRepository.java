package vn.xuanthai.clinic.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.xuanthai.clinic.booking.entity.Permission;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    // Dùng cho việc "gieo dữ liệu" (DataSeeder)
    Optional<Permission> findByName(String name);
}
