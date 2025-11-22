package vn.xuanthai.clinic.booking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vn.xuanthai.clinic.booking.enums.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
public class DoctorCreateRequest {

    @NotNull(message = "ID của tài khoản User không được để trống")
    private Long userId; // ID của tài khoản User (đã được tạo trước)

    @NotNull(message = "ID chuyên khoa không được để trống")
    private Long specialtyId;

    @NotNull(message = "ID phòng khám không được để trống")
    private Long clinicId;

    private String description;
    private String academicDegree;
    private BigDecimal price;
    private Gender gender;
    private LocalDate birthday;
    private String image; // Avatar
    private Set<String> otherImages; // Ảnh bằng cấp/chứng chỉ
}