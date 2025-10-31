package vn.xuanthai.clinic.booking.dto.response;

import lombok.Data;
import vn.xuanthai.clinic.booking.enums.AppointmentStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AppointmentResponse {
    private Long id;
    private String patientName;
    private String doctorName;
    private String specialtyName;
    private String clinicName;
    private LocalDate date;
    private String timeSlot;
    private String reason;
    private AppointmentStatus status;
    private LocalDateTime createdAt;
}