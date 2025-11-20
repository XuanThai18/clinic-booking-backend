package vn.xuanthai.clinic.booking.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.xuanthai.clinic.booking.dto.request.SpecialtyRequest;
import vn.xuanthai.clinic.booking.dto.response.SpecialtyResponse;
import vn.xuanthai.clinic.booking.entity.Specialty;
import vn.xuanthai.clinic.booking.exception.BadRequestException;
import vn.xuanthai.clinic.booking.exception.ResourceNotFoundException;
import vn.xuanthai.clinic.booking.repository.SpecialtyRepository;
import vn.xuanthai.clinic.booking.service.IFileService;
import vn.xuanthai.clinic.booking.service.ISpecialtyService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecialtyServiceImpl implements ISpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final IFileService fileService;

    @Override
    public SpecialtyResponse createSpecialty(SpecialtyRequest request) {
        // 1. Chuyển đổi DTO sang Entity
        Specialty newSpecialty = new Specialty();
        newSpecialty.setName(request.getName());
        newSpecialty.setDescription(request.getDescription());
        // Gán danh sách ảnh
        newSpecialty.setImageUrls(request.getImageUrls());

        Specialty savedSpecialty = specialtyRepository.save(newSpecialty);
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

        // xóa ảnh nếu bị gơx bỏ khi update
        //  Lấy danh sách ảnh hiện tại trong DB
        Set<String> currentImages = new HashSet<>(existingSpecialty.getImageUrls());

        //  Lấy danh sách ảnh mới từ Request
        Set<String> newImages = request.getImageUrls(); // Đây là danh sách cuối cùng user muốn giữ

        //  Tìm những ảnh có trong 'current' nhưng KHÔNG có trong 'new'
        // (Tức là những ảnh user đã bấm dấu X để xóa)
        for (String oldUrl : currentImages) {
            if (!newImages.contains(oldUrl)) {
                // Ảnh này đã bị loại bỏ -> Xóa trên Cloudinary
                fileService.deleteFile(oldUrl);
            }
        }

        // 2. Cập nhật thông tin từ request
        existingSpecialty.setName(request.getName());
        existingSpecialty.setDescription(request.getDescription());
        existingSpecialty.setImageUrls(request.getImageUrls());

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

        if (existingSpecialty.getImageUrls() != null) {
            for (String url : existingSpecialty.getImageUrls()) {
                fileService.deleteFile(url);
            }
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
        dto.setImageUrls(specialty.getImageUrls());
        return dto;
    }
}

