package vn.xuanthai.clinic.booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentCancelledEvent {
    private String recipientEmail; // Email người nhận (Bệnh nhân)
    private String patientName;
    private String doctorName;
    private LocalDate appointmentDate;
    private String timeSlot;
    private String reason; // (Tùy chọn) Lý do hủy
}
