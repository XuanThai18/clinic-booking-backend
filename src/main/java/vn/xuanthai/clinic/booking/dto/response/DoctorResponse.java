package vn.xuanthai.clinic.booking.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DoctorResponse {
    private Long doctorId; // ID của hồ sơ bác sĩ
    private Long userId; // ID của tài khoản user liên kết
    private String fullName;
    private String email;
    private String description;
    private String academicDegree;
    private BigDecimal price;

    // Chúng ta sẽ trả về thông tin chi tiết của chuyên khoa và phòng khám
    private SpecialtyResponse specialty;
    private ClinicResponse clinic;
}