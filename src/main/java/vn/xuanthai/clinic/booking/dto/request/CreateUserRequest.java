package vn.xuanthai.clinic.booking.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import vn.xuanthai.clinic.booking.enums.Gender;

import java.time.LocalDate;
import java.util.Set;

@Data
public class CreateUserRequest {

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 12, message = "Mật khẩu phải có ít nhất 12 ký tự")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
            message = "Mật khẩu phải chứa ít nhất một chữ hoa, một chữ thường, một số, và một ký tự đặc biệt (@#$%^&+=!)"
    )
    private String password;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phoneNumber;

    private String address;

    private Gender gender;

    private LocalDate birthday;

    private Boolean isActive;

    // Đây là phần quan trọng nhất: Admin sẽ gửi lên một danh sách TÊN của các role
    @NotEmpty(message = "Người dùng phải có ít nhất một vai trò")
    private Set<String> roles; // Ví dụ: ["ROLE_ADMIN", "ROLE_DOCTOR"]
}