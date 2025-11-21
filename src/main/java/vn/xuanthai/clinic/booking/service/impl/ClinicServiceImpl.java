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
import vn.xuanthai.clinic.booking.service.IFileService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClinicServiceImpl implements IClinicService {

    private final ClinicRepository clinicRepository;
    private final IFileService fileService;

    @Override
    public ClinicResponse createClinic(ClinicRequest request) {
        Clinic newClinic = new Clinic();
        newClinic.setName(request.getName());
        newClinic.setAddress(request.getAddress());
        newClinic.setPhoneNumber(request.getPhoneNumber());
        newClinic.setDescription(request.getDescription());

        // 3. Gán danh sách ảnh
        newClinic.setImageUrls(request.getImageUrls());

        Clinic savedClinic = clinicRepository.save(newClinic);
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

        // --- 4. LOGIC XÓA ẢNH RÁC TRÊN CLOUDINARY ---
        Set<String> oldImages = new HashSet<>(existingClinic.getImageUrls());
        Set<String> newImages = request.getImageUrls();

        // Ảnh nào có trong cũ mà không có trong mới -> Xóa
        for (String oldUrl : oldImages) {
            if (newImages == null || !newImages.contains(oldUrl)) {
                fileService.deleteFile(oldUrl);
            }
        }
        // ----------------------------------------------

        existingClinic.setName(request.getName());
        existingClinic.setAddress(request.getAddress());
        existingClinic.setPhoneNumber(request.getPhoneNumber());
        existingClinic.setDescription(request.getDescription());

        // Cập nhật danh sách ảnh mới
        existingClinic.setImageUrls(request.getImageUrls());

        Clinic updatedClinic = clinicRepository.save(existingClinic);
        return mapToResponse(updatedClinic);
    }

    @Override
    public void deleteClinic(Long clinicId) {
        Clinic existingClinic = findClinicById(clinicId);

        if (existingClinic.getDoctors() != null && !existingClinic.getDoctors().isEmpty()) {
            throw new BadRequestException("Không thể xóa phòng khám đang có bác sĩ làm việc.");
        }

        // --- 5. XÓA TẤT CẢ ẢNH TRÊN CLOUDINARY ---
        if (existingClinic.getImageUrls() != null) {
            for (String url : existingClinic.getImageUrls()) {
                fileService.deleteFile(url);
            }
        }

        clinicRepository.delete(existingClinic);
    }

    // --- Phương thức trợ giúp ---

    private Clinic findClinicById(Long clinicId) {
        return clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy phòng khám với ID: " + clinicId));
    }

    private ClinicResponse mapToResponse(Clinic clinic) {
        ClinicResponse dto = new ClinicResponse();
        dto.setId(clinic.getId());
        dto.setName(clinic.getName());
        dto.setAddress(clinic.getAddress());
        dto.setPhoneNumber(clinic.getPhoneNumber());
        dto.setDescription(clinic.getDescription());

        // 6. Map danh sách ảnh
        dto.setImageUrls(clinic.getImageUrls());

        return dto;
    }
}