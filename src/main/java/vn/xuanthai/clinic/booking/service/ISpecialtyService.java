package vn.xuanthai.clinic.booking.service;

import vn.xuanthai.clinic.booking.dto.request.SpecialtyRequest;
import vn.xuanthai.clinic.booking.dto.response.SpecialtyResponse;

import java.util.List;

public interface ISpecialtyService {

    // Tạo mới một chuyên khoa
    SpecialtyResponse createSpecialty(SpecialtyRequest request);

    // Lấy thông tin một chuyên khoa bằng ID
    SpecialtyResponse getSpecialtyById(Long specialtyId);

    // Lấy tất cả chuyên khoa
    List<SpecialtyResponse> getAllSpecialties();

    // Cập nhật một chuyên khoa
    SpecialtyResponse updateSpecialty(Long specialtyId, SpecialtyRequest request);

    // Xóa một chuyên khoa
    void deleteSpecialty(Long specialtyId);
}
