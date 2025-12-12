package vn.xuanthai.clinic.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class SpecialtyRequest {

    @NotBlank(message = "Tên chuyên khoa không được để trống")
    private String name;

    private String description;
    private Set<String> imageUrls;
}