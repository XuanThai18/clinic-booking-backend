package vn.xuanthai.clinic.booking.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.xuanthai.clinic.booking.dto.request.DoctorCreateRequest;
import vn.xuanthai.clinic.booking.dto.response.ClinicResponse;
import vn.xuanthai.clinic.booking.dto.response.DoctorResponse;
import vn.xuanthai.clinic.booking.dto.response.SpecialtyResponse;
import vn.xuanthai.clinic.booking.entity.Clinic;
import vn.xuanthai.clinic.booking.entity.Doctor;
import vn.xuanthai.clinic.booking.entity.Specialty;
import vn.xuanthai.clinic.booking.entity.User;
import vn.xuanthai.clinic.booking.exception.ResourceNotFoundException;
import vn.xuanthai.clinic.booking.repository.ClinicRepository;
import vn.xuanthai.clinic.booking.repository.DoctorRepository;
import vn.xuanthai.clinic.booking.repository.SpecialtyRepository;
import vn.xuanthai.clinic.booking.repository.UserRepository;
import vn.xuanthai.clinic.booking.service.IDoctorService;
import vn.xuanthai.clinic.booking.service.IFileService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements IDoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final SpecialtyRepository specialtyRepository;
    private final ClinicRepository clinicRepository;
    private final IFileService fileService;

    @Override
    @Transactional // Đảm bảo tất cả cùng thành công hoặc thất bại
    public DoctorResponse createDoctorProfile(DoctorCreateRequest request) {
        // 1. Tìm các đối tượng liên quan
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy User với ID: " + request.getUserId()));

        Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Chuyên khoa với ID: " + request.getSpecialtyId()));

        Clinic clinic = clinicRepository.findById(request.getClinicId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Phòng khám với ID: " + request.getClinicId()));

        // 2. Tạo Entity Doctor mới
        Doctor newDoctor = new Doctor();
        newDoctor.setUser(user);
        newDoctor.setSpecialty(specialty);
        newDoctor.setClinic(clinic);
        newDoctor.setDescription(request.getDescription());
        newDoctor.setAcademicDegree(request.getAcademicDegree());
        newDoctor.setPrice(request.getPrice());

        // --- CẬP NHẬT PHẦN ẢNH ---
        newDoctor.setImage(request.getImage());             // Lưu Avatar (String)
        newDoctor.setOtherImages(request.getOtherImages()); // Lưu danh sách ảnh bằng cấp (Set<String>)
        // --------------------------

        // 3. Lưu vào CSDL
        Doctor savedDoctor = doctorRepository.save(newDoctor);

        // 4. Ánh xạ sang DTO để trả về
        return mapToDoctorResponse(savedDoctor);
    }

    @Override
    public DoctorResponse getDoctorById(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Bác sĩ với ID: " + doctorId));
        return mapToDoctorResponse(doctor);
    }

    @Override
    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::mapToDoctorResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorResponse> findDoctorsBySpecialty(Long specialtyId) {
        return doctorRepository.findBySpecialtyId(specialtyId).stream()
                .map(this::mapToDoctorResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DoctorResponse updateDoctorProfile(Long doctorId, DoctorCreateRequest request) {
        // 1. Tìm bác sĩ cũ
        Doctor existingDoctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bác sĩ với ID: " + doctorId));

        // 2. Cập nhật thông tin cơ bản
        existingDoctor.setDescription(request.getDescription());
        existingDoctor.setAcademicDegree(request.getAcademicDegree());
        existingDoctor.setPrice(request.getPrice());

        // 3. Cập nhật các mối quan hệ (nếu thay đổi)
        if (!existingDoctor.getSpecialty().getId().equals(request.getSpecialtyId())) {
            Specialty newSpecialty = specialtyRepository.findById(request.getSpecialtyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chuyên khoa không tồn tại"));
            existingDoctor.setSpecialty(newSpecialty);
        }

        if (!existingDoctor.getClinic().getId().equals(request.getClinicId())) {
            Clinic newClinic = clinicRepository.findById(request.getClinicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Phòng khám không tồn tại"));
            existingDoctor.setClinic(newClinic);
        }

        // (User thường không cho đổi, nên ta bỏ qua hoặc kiểm tra nếu cần)

        // 4. Xử lý Avatar (Xóa ảnh cũ nếu đổi ảnh mới)
        String oldAvatar = existingDoctor.getImage();
        String newAvatar = request.getImage();
        if (oldAvatar != null && !oldAvatar.equals(newAvatar)) {
            fileService.deleteFile(oldAvatar); // Xóa trên Cloudinary
        }
        existingDoctor.setImage(newAvatar);

        // 5. Xử lý Ảnh Chứng chỉ (Xóa ảnh rác)
        Set<String> oldImages = new HashSet<>(existingDoctor.getOtherImages());
        Set<String> newImages = request.getOtherImages();

        if (oldImages != null) {
            for (String oldUrl : oldImages) {
                if (newImages == null || !newImages.contains(oldUrl)) {
                    fileService.deleteFile(oldUrl); // Xóa những ảnh không còn trong danh sách mới
                }
            }
        }
        existingDoctor.setOtherImages(newImages);

        // 6. Lưu và trả về
        Doctor updatedDoctor = doctorRepository.save(existingDoctor);
        return mapToDoctorResponse(updatedDoctor);
    }

    @Override
    @Transactional
    public void deleteDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bác sĩ với ID: " + doctorId));

        // 1. Xóa Avatar
        if (doctor.getImage() != null) {
            fileService.deleteFile(doctor.getImage());
        }

        // 2. Xóa Chứng chỉ
        if (doctor.getOtherImages() != null) {
            for (String url : doctor.getOtherImages()) {
                fileService.deleteFile(url);
            }
        }

        // 3. Xóa trong DB
        doctorRepository.delete(doctor);
    }

    // --- Phương thức trợ giúp để ánh xạ ---
    private DoctorResponse mapToDoctorResponse(Doctor doctor) {
        DoctorResponse dto = new DoctorResponse();
        dto.setDoctorId(doctor.getId());
        dto.setUserId(doctor.getUser().getId());
        dto.setFullName(doctor.getUser().getFullName());
        dto.setEmail(doctor.getUser().getEmail());
        dto.setDescription(doctor.getDescription());
        dto.setAcademicDegree(doctor.getAcademicDegree());
        dto.setPrice(doctor.getPrice());

        // --- CẬP NHẬT PHẦN ẢNH BÁC SĨ ---
        dto.setImage(doctor.getImage());             // Trả về Avatar
        dto.setOtherImages(doctor.getOtherImages()); // Trả về danh sách ảnh khác
        // --------------------------------

        // Ánh xạ thông tin chuyên khoa (Cập nhật logic ảnh)
        if (doctor.getSpecialty() != null) {
            SpecialtyResponse specialtyDto = new SpecialtyResponse();
            specialtyDto.setId(doctor.getSpecialty().getId());
            specialtyDto.setName(doctor.getSpecialty().getName());
            specialtyDto.setDescription(doctor.getSpecialty().getDescription());
            // Lưu ý: Specialty giờ dùng imageUrls (Set), không phải imageUrl (String)
            specialtyDto.setImageUrls(doctor.getSpecialty().getImageUrls());
            dto.setSpecialty(specialtyDto);
        }

        // Ánh xạ thông tin phòng khám (Cập nhật logic ảnh)
        if (doctor.getClinic() != null) {
            ClinicResponse clinicDto = new ClinicResponse();
            clinicDto.setId(doctor.getClinic().getId());
            clinicDto.setName(doctor.getClinic().getName());
            clinicDto.setAddress(doctor.getClinic().getAddress());
            clinicDto.setPhoneNumber(doctor.getClinic().getPhoneNumber());
            clinicDto.setDescription(doctor.getClinic().getDescription());
            // Lưu ý: Clinic giờ dùng imageUrls (Set)
            clinicDto.setImageUrls(doctor.getClinic().getImageUrls());
            dto.setClinic(clinicDto);
        }

        return dto;
    }
}