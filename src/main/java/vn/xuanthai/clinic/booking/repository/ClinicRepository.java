package vn.xuanthai.clinic.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.xuanthai.clinic.booking.entity.Clinic;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Long> {
    // Các phương thức CRUD cơ bản đã đủ dùng cho các tính năng hiện tại
}
