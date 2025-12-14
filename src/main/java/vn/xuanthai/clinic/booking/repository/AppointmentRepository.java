package vn.xuanthai.clinic.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.xuanthai.clinic.booking.entity.Appointment;
import vn.xuanthai.clinic.booking.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Tìm lịch hẹn của bệnh nhân
    List<Appointment> findAllByPatientId(Long patientId);

    // Tìm tất cả lịch hẹn thuộc về một phòng khám cụ thể
    List<Appointment> findAllBySchedule_Doctor_Clinic_Id(Long clinicId);

    // Tìm lịch hẹn dựa trên ID bác sĩ (đi đường vòng qua Schedule)
    List<Appointment> findAllBySchedule_Doctor_Id(Long doctorId);

    // Tìm các đơn có trạng thái là status VÀ được tạo trước thời điểm time
    List<Appointment> findByStatusAndCreatedAtBefore(AppointmentStatus status, LocalDateTime time);
}
