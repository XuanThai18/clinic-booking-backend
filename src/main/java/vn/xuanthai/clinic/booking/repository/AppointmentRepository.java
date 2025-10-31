package vn.xuanthai.clinic.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.xuanthai.clinic.booking.entity.Appointment;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Tìm tất cả các lịch hẹn của một bệnh nhân
    List<Appointment> findByPatientId(Long patientId);

    // Tìm tất cả các lịch hẹn liên quan đến một bác sĩ (thông qua schedule)
    List<Appointment> findByScheduleDoctorId(Long doctorId);
}
