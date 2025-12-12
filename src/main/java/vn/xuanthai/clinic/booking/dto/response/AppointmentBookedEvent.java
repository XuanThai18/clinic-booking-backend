package vn.xuanthai.clinic.booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentBookedEvent {
    private Long appointmentId;
    private String patientEmail;    // Để gửi mail
    private String patientName;     // Để xưng hô trong mail
    private String doctorName;
    private LocalDate date;
    private String timeSlot;
}