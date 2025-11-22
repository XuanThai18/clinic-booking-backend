package vn.xuanthai.clinic.booking.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class UserResponsePage {
    private List<UserResponse> content; // Danh sách user trong trang hiện tại
    private int pageNo;                 // Trang số mấy (0, 1, 2...)
    private int pageSize;               // Số lượng user mỗi trang
    private long totalElements;         // Tổng số user tìm thấy
    private int totalPages;             // Tổng số trang
    private boolean last;               // Có phải trang cuối không?
}