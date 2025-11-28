package vn.xuanthai.clinic.booking.dto.response;

import lombok.Data;
import vn.xuanthai.clinic.booking.enums.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
public class DoctorResponse {
    private Long doctorId; // ID của hồ sơ bác sĩ
    private Long userId; // ID của tài khoản user liên kết
    private String fullName;
    private String email;
    private String description;
    private String academicDegree;
    private BigDecimal price;
    private String phoneNumber;
    private String address;
    private Gender gender;
    private LocalDate birthday;
    private String image; // Avatar
    private Set<String> otherImages; // Ảnh bằng cấp/chứng chỉ

    // Chúng ta sẽ trả về thông tin chi tiết của chuyên khoa và phòng khám
    private SpecialtyResponse specialty;
    private ClinicResponse clinic;
}