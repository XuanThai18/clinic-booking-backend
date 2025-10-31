package vn.xuanthai.clinic.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.xuanthai.clinic.booking.entity.Schedule;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // Tìm lịch làm việc của một bác sĩ trong một ngày, có phân trang
    Page<Schedule> findByDoctorIdAndDate(Long doctorId, LocalDate date, Pageable pageable);

    // Tìm lịch làm việc của một bác sĩ trong một khoảng thời gian
    List<Schedule> findByDoctorIdAndDateBetween(Long doctorId, LocalDate startDate, LocalDate endDate);
}
