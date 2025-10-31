package vn.xuanthai.clinic.booking.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DoctorResponse {
    private Long id;
    private String fullName; // Lấy từ User liên quan
    private String email;    // Lấy từ User liên quan
    private String description;
    private String academicDegree;
    private BigDecimal price;
    private String specialtyName; // Chỉ lấy tên chuyên khoa
    private String clinicName;    // Chỉ lấy tên phòng khám
}