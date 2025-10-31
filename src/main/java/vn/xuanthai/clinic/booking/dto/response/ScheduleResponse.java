package vn.xuanthai.clinic.booking.dto.response;

import lombok.Data;
import vn.xuanthai.clinic.booking.enums.ScheduleStatus;
import java.time.LocalDate;

@Data
public class ScheduleResponse {
    private Long id;
    private LocalDate date;
    private String timeSlot;
    private ScheduleStatus status;
}