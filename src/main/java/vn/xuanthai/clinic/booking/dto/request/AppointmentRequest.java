package vn.xuanthai.clinic.booking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentRequest {
    @NotNull(message = "ID của khung giờ không được để trống")
    private Long scheduleId;

    private String reason;
}