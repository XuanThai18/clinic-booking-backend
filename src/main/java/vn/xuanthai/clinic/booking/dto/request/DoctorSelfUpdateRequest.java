package vn.xuanthai.clinic.booking.dto.request;

import lombok.Data;

import java.util.Set;

@Data
public class DoctorSelfUpdateRequest {
    private String description;     // Cho phép sửa mô tả
    private String academicDegree;  // Cho phép sửa học vị
    private String image;           // Cho phép đổi avatar
    private Set<String> otherImages; // Cho phép đổi ảnh bằng cấp
}