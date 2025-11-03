package vn.xuanthai.clinic.booking.service;

import vn.xuanthai.clinic.booking.dto.request.DoctorCreateRequest;
import vn.xuanthai.clinic.booking.dto.response.DoctorResponse;
import java.util.List;

public interface IDoctorService {

    DoctorResponse createDoctorProfile(DoctorCreateRequest request);

    DoctorResponse getDoctorById(Long doctorId);

    // Lấy danh sách bác sĩ (có thể thêm phân trang, lọc sau)
    List<DoctorResponse> getAllDoctors();

    // Nâng cao: Tìm bác sĩ theo chuyên khoa
    List<DoctorResponse> findDoctorsBySpecialty(Long specialtyId);

    // (Em có thể tự viết thêm hàm update và delete)
}