package vn.xuanthai.clinic.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.xuanthai.clinic.booking.entity.Schedule;
import vn.xuanthai.clinic.booking.enums.ScheduleStatus;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // Tìm lịch làm việc của một bác sĩ trong một ngày, có phân trang
    Page<Schedule> findByDoctorIdAndDate(Long doctorId, LocalDate date, Pageable pageable);

    // Tìm lịch làm việc của một bác sĩ trong một khoảng thời gian
    List<Schedule> findByDoctorIdAndDateBetween(Long doctorId, LocalDate startDate, LocalDate endDate);

    // Tìm tất cả lịch (cả đã đặt và còn trống) của bác sĩ theo ngày
    List<Schedule> findByDoctorIdAndDate(Long doctorId, LocalDate date);

    // Tìm lịch chính xác của 1 ngày (Và nhớ sắp xếp theo giờ tăng dần 7h->9h->10h...)
    List<Schedule> findByDoctorIdAndDateOrderByTimeSlotAsc(Long doctorId, LocalDate date);

    // Chỉ tìm các lịch CÒN TRỐNG của bác sĩ theo ngày
    List<Schedule> findByDoctorIdAndDateAndStatus(Long doctorId, LocalDate date, ScheduleStatus status);

    // Lấy danh sách các ngày có lịch trong khoảng thời gian (không trùng lặp)
    @Query("SELECT DISTINCT s.date FROM Schedule s WHERE s.doctor.id = :doctorId AND s.date BETWEEN :startDate AND :endDate")
    List<LocalDate> findDistinctDatesByDoctorIdAndDateBetween(
            @Param("doctorId") Long doctorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}