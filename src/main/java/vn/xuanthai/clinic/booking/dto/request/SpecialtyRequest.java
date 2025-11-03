package vn.xuanthai.clinic.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SpecialtyRequest {

    @NotBlank(message = "Tên chuyên khoa không được để trống")
    private String name;

    // Các trường này có thể là tùy chọn (nullable)
    private String description;
    private String imageUrl;
}