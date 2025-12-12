package vn.xuanthai.clinic.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class ClinicRequest {

    @NotBlank(message = "Tên phòng khám không được để trống")
    private String name;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    private String phoneNumber;
    private String description;
    private Set<String> imageUrls;
}