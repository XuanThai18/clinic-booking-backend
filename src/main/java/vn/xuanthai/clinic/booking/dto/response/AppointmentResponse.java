package vn.xuanthai.clinic.booking.dto.response;

import lombok.Data;
import vn.xuanthai.clinic.booking.enums.AppointmentStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AppointmentResponse {
    private Long id;
    private LocalDateTime createdAt;
    private AppointmentStatus status;
    private String reason;

    // Thông tin bệnh nhân
    private Long patientId;
    private String patientName;

    // Thông tin bác sĩ
    private Long doctorId;
    private String doctorName;

    // Thông tin phòng khám
    private String clinicName;

    private String specialtyName;

    // Thông tin lịch hẹn
    private LocalDate appointmentDate;
    private String appointmentTimeSlot;

    private String diagnosis;    // Chẩn đoán
    private String prescription; // Đơn thuốc/Ghi chú

    private String patientPhone;
}