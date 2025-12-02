package vn.xuanthai.clinic.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompletionRequest {

    @NotBlank(message = "Vui lòng nhập chẩn đoán bệnh")
    private String diagnosis; // Chẩn đoán

    private String prescription; // Đơn thuốc / Ghi chú (có thể để trống nếu chỉ tư vấn)
}