package vn.xuanthai.clinic.booking.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.xuanthai.clinic.booking.dto.request.ClinicRequest;
import vn.xuanthai.clinic.booking.dto.response.ClinicResponse;
import vn.xuanthai.clinic.booking.entity.Clinic;
import vn.xuanthai.clinic.booking.exception.BadRequestException;
import vn.xuanthai.clinic.booking.exception.ResourceNotFoundException;
import vn.xuanthai.clinic.booking.repository.ClinicRepository;
import vn.xuanthai.clinic.booking.service.IClinicService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClinicServiceImpl implements IClinicService {

    private final ClinicRepository clinicRepository;

    @Override
    public ClinicResponse createClinic(ClinicRequest request) {
        // Ánh xạ từ DTO Request sang Entity
        Clinic newClinic = new Clinic();
        newClinic.setName(request.getName());
        newClinic.setAddress(request.getAddress());
        newClinic.setPhoneNumber(request.getPhoneNumber());
        newClinic.setDescription(request.getDescription());
        newClinic.setImageUrl(request.getImageUrl());

        Clinic savedClinic = clinicRepository.save(newClinic);

        // Ánh xạ từ Entity sang DTO Response
        return mapToResponse(savedClinic);
    }

    @Override
    public ClinicResponse getClinicById(Long clinicId) {
        Clinic clinic = findClinicById(clinicId);
        return mapToResponse(clinic);
    }

    @Override
    public List<ClinicResponse> getAllClinics() {
        return clinicRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ClinicResponse updateClinic(Long clinicId, ClinicRequest request) {
        Clinic existingClinic = findClinicById(clinicId);

        // Cập nhật các trường
        existingClinic.setName(request.getName());
        existingClinic.setAddress(request.getAddress());
        existingClinic.setPhoneNumber(request.getPhoneNumber());
        existingClinic.setDescription(request.getDescription());
        existingClinic.setImageUrl(request.getImageUrl());

        Clinic updatedClinic = clinicRepository.save(existingClinic);
        return mapToResponse(updatedClinic);
    }

    @Override
    public void deleteClinic(Long clinicId) {
        Clinic existingClinic = findClinicById(clinicId);

        // Kiểm tra logic nghiệp vụ: Không cho xóa nếu vẫn còn bác sĩ
        if (existingClinic.getDoctors() != null && !existingClinic.getDoctors().isEmpty()) {
            throw new BadRequestException("Không thể xóa phòng khám đang có bác sĩ làm việc.");
        }

        clinicRepository.delete(existingClinic);
    }

    // --- Phương thức trợ giúp ---

    // Tìm phòng khám hoặc ném lỗi 404
    private Clinic findClinicById(Long clinicId) {
        return clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy phòng khám với ID: " + clinicId));
    }

    // Ánh xạ từ Entity sang DTO Response
    private ClinicResponse mapToResponse(Clinic clinic) {
        ClinicResponse dto = new ClinicResponse();
        dto.setId(clinic.getId());
        dto.setName(clinic.getName());
        dto.setAddress(clinic.getAddress());
        dto.setPhoneNumber(clinic.getPhoneNumber());
        dto.setDescription(clinic.getDescription());
        dto.setImageUrl(clinic.getImageUrl());
        return dto;
    }
}