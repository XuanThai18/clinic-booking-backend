package vn.xuanthai.clinic.booking.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import vn.xuanthai.clinic.booking.enums.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
public class DoctorRegistrationRequest {

    // --- PHẦN 1: THÔNG TIN TÀI KHOẢN (USER) ---
    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 12, message = "Mật khẩu phải có ít nhất 12 ký tự")
    private String password;

    private String phoneNumber;
    private String address;
    private Gender gender;
    private LocalDate birthday;

    // --- PHẦN 2: THÔNG TIN CHUYÊN MÔN (DOCTOR PROFILE) ---
    @NotNull(message = "ID Chuyên khoa không được để trống")
    private Long specialtyId;

    @NotNull(message = "ID Phòng khám không được để trống")
    private Long clinicId;

    private String description;
    private String academicDegree;
    private BigDecimal price;

    // --- PHẦN 3: ẢNH ---
    private String image; // Avatar
    private Set<String> otherImages; // Ảnh bằng cấp
}