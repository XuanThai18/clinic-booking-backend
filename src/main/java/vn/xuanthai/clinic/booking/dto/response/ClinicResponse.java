package vn.xuanthai.clinic.booking.dto.response;

import lombok.Data;

import java.util.Set;

@Data
public class ClinicResponse {
    private Long id;
    private String name;
    private String address;
    private String phoneNumber;
    private String description;
    private Set<String> imageUrls;

    // Lưu ý: Chúng ta không trả về Set<Doctor> ở đây
    // để tránh lỗi lặp JSON và giữ cho DTO này gọn nhẹ.
    // Nếu cần danh sách bác sĩ của phòng khám, ta sẽ tạo một API riêng.
}