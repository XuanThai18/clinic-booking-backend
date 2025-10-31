package vn.xuanthai.clinic.booking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentRequest {
    @NotNull(message = "ID của khung giờ không được để trống")
    private Long scheduleId;

    // patientId sẽ được lấy từ người dùng đang đăng nhập, không cần client gửi lên

    private String reason;
}