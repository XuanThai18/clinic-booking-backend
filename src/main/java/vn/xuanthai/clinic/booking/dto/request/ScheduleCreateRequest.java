package vn.xuanthai.clinic.booking.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.Set;

@Data
public class ScheduleCreateRequest {

    @NotNull(message = "ID Bác sĩ không được để trống")
    private Long doctorId;

    @NotNull(message = "Ngày làm việc không được để trống")
    @FutureOrPresent(message = "Không thể tạo lịch cho ngày trong quá khứ")
    private LocalDate date;

    @NotEmpty(message = "Phải có ít nhất một khung giờ làm việc")
    private Set<String> timeSlots; // Ví dụ: ["08:00", "08:30", "09:00"]
}