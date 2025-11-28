package vn.xuanthai.clinic.booking.service;

import vn.xuanthai.clinic.booking.dto.request.DoctorCreateRequest;
import vn.xuanthai.clinic.booking.dto.request.DoctorRegistrationRequest;
import vn.xuanthai.clinic.booking.dto.response.DoctorResponse;
import java.util.List;

public interface IDoctorService {

    DoctorResponse createDoctorProfile(DoctorCreateRequest request);

    // Hàm mới: Đăng ký trọn gói
    DoctorResponse registerDoctor(DoctorRegistrationRequest request);

    DoctorResponse getDoctorById(Long doctorId);

    // Lấy danh sách bác sĩ (có thể thêm phân trang, lọc sau)
    List<DoctorResponse> getAllDoctors();

    // Hàm lấy hồ sơ của chính bác sĩ đang đăng nhập
    DoctorResponse getMyDoctorProfile();

    // Nâng cao: Tìm bác sĩ theo chuyên khoa
    List<DoctorResponse> findDoctorsBySpecialty(Long specialtyId);

    DoctorResponse updateDoctorProfile(Long doctorId, DoctorCreateRequest request);
    void deleteDoctor(Long doctorId);
}