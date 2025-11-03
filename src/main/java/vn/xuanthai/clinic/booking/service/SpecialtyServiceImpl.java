package vn.xuanthai.clinic.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.xuanthai.clinic.booking.dto.request.SpecialtyRequest;
import vn.xuanthai.clinic.booking.dto.response.SpecialtyResponse;
import vn.xuanthai.clinic.booking.entity.Specialty;
import vn.xuanthai.clinic.booking.exception.BadRequestException;
import vn.xuanthai.clinic.booking.exception.ResourceNotFoundException;
import vn.xuanthai.clinic.booking.repository.SpecialtyRepository;
import vn.xuanthai.clinic.booking.service.ISpecialtyService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecialtyServiceImpl implements ISpecialtyService {

    private final SpecialtyRepository specialtyRepository;

    @Override
    public SpecialtyResponse createSpecialty(SpecialtyRequest request) {
        // 1. Chuyển đổi DTO sang Entity
        Specialty newSpecialty = new Specialty();
        newSpecialty.setName(request.getName());
        newSpecialty.setDescription(request.getDescription());
        newSpecialty.setImageUrl(request.getImageUrl());

        // 2. Lưu vào CSDL
        Specialty savedSpecialty = specialtyRepository.save(newSpecialty);

        // 3. Chuyển đổi Entity sang DTO Response để trả về
        return mapToResponse(savedSpecialty);
    }

    @Override
    public SpecialtyResponse getSpecialtyById(Long specialtyId) {
        Specialty specialty = findSpecialtyById(specialtyId);
        return mapToResponse(specialty);
    }

    @Override
    public List<SpecialtyResponse> getAllSpecialties() {
        return specialtyRepository.findAll().stream()
                .map(this::mapToResponse) // Dùng method reference
                .collect(Collectors.toList());
    }

    @Override
    public SpecialtyResponse updateSpecialty(Long specialtyId, SpecialtyRequest request) {
        // 1. Tìm chuyên khoa hiện tại
        Specialty existingSpecialty = findSpecialtyById(specialtyId);

        // 2. Cập nhật thông tin từ request
        existingSpecialty.setName(request.getName());
        existingSpecialty.setDescription(request.getDescription());
        existingSpecialty.setImageUrl(request.getImageUrl());

        // 3. Lưu lại
        Specialty updatedSpecialty = specialtyRepository.save(existingSpecialty);
        return mapToResponse(updatedSpecialty);
    }

    @Override
    public void deleteSpecialty(Long specialtyId) {
        Specialty existingSpecialty = findSpecialtyById(specialtyId);

         if (!existingSpecialty.getDoctors().isEmpty()) {
            throw new BadRequestException("Không thể xóa chuyên khoa đã có bác sĩ.");
         }

        specialtyRepository.delete(existingSpecialty);
    }

    // --- Phương thức trợ giúp ---

    // Tìm chuyên khoa hoặc ném lỗi 404
    private Specialty findSpecialtyById(Long specialtyId) {
        return specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy chuyên khoa với ID: " + specialtyId));
    }

    // Ánh xạ từ Entity sang DTO Response
    private SpecialtyResponse mapToResponse(Specialty specialty) {
        SpecialtyResponse dto = new SpecialtyResponse();
        dto.setId(specialty.getId());
        dto.setName(specialty.getName());
        dto.setDescription(specialty.getDescription());
        dto.setImageUrl(specialty.getImageUrl());
        return dto;
    }
}

