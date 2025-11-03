package vn.xuanthai.clinic.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClinicRequest {

    @NotBlank(message = "Tên phòng khám không được để trống")
    private String name;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    // Các trường này có thể là tùy chọn (nullable)
    private String phoneNumber;
    private String description;
    private String imageUrl; // Sẽ nhận URL từ API upload file
}