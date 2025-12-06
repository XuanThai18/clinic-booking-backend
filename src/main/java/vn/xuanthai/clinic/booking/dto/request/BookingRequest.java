package vn.xuanthai.clinic.booking.dto.request;

import lombok.Data;

@Data
public class BookingRequest {
    private Long doctorId;
    private Long scheduleId;
    private String reason;
}