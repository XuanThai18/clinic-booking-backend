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
        // 1. Tìm User
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy User với ID: " + request.getUserId()));

        // --- BƯỚC MỚI: CẬP NHẬT THÔNG TIN CÁ NHÂN VÀO USER ---
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday());
        }
        // Hibernate sẽ tự động lưu thay đổi của user khi transaction kết thúc
        // -----------------------------------------------------

        // 2. Tìm Chuyên khoa và Phòng khám
        Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Chuyên khoa với ID: " + request.getSpecialtyId()));

        Clinic clinic = clinicRepository.findById(request.getClinicId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Phòng khám với ID: " + request.getClinicId()));

        // 3. Tạo Entity Doctor mới
        Doctor newDoctor = new Doctor();
        newDoctor.setUser(user);
        newDoctor.setSpecialty(specialty);
        newDoctor.setClinic(clinic);
        newDoctor.setDescription(request.getDescription());
        newDoctor.setAcademicDegree(request.getAcademicDegree());
        newDoctor.setPrice(request.getPrice());

        // Lưu ảnh
        newDoctor.setImage(request.getImage());             // Lưu Avatar
        newDoctor.setOtherImages(request.getOtherImages()); // Lưu danh sách ảnh chứng chỉ

        // 4. Lưu vào CSDL
        Doctor savedDoctor = doctorRepository.save(newDoctor);

        // 5. Ánh xạ sang DTO để trả về
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

        // 2. Cập nhật thông tin cá nhân vào User (Nếu có thay đổi)
        User user = existingDoctor.getUser();
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getBirthday() != null) user.setBirthday(request.getBirthday());

        // 3. Cập nhật thông tin cơ bản bác sĩ
        existingDoctor.setDescription(request.getDescription());
        existingDoctor.setAcademicDegree(request.getAcademicDegree());
        existingDoctor.setPrice(request.getPrice());

        // 4. Cập nhật quan hệ (nếu thay đổi)
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

        // 5. Xử lý Avatar (Xóa ảnh cũ nếu đổi ảnh mới)
        String oldAvatar = existingDoctor.getImage();
        String newAvatar = request.getImage();
        // Nếu ảnh cũ khác null VÀ (ảnh mới khác null và khác ảnh cũ)
        if (oldAvatar != null && newAvatar != null && !oldAvatar.equals(newAvatar)) {
            fileService.deleteFile(oldAvatar); // Xóa trên Cloudinary
        }
        existingDoctor.setImage(newAvatar);

        // 6. Xử lý Ảnh Chứng chỉ (Xóa ảnh rác)
        Set<String> oldImages = new HashSet<>(existingDoctor.getOtherImages());
        Set<String> newImages = request.getOtherImages();

        if (oldImages != null && newImages != null) {
            for (String oldUrl : oldImages) {
                // Nếu ảnh cũ không còn nằm trong danh sách mới -> Xóa
                if (!newImages.contains(oldUrl)) {
                    fileService.deleteFile(oldUrl);
                }
            }
        }
        existingDoctor.setOtherImages(newImages);

        // 7. Lưu và trả về
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

        // Thông tin từ User
        dto.setFullName(doctor.getUser().getFullName());
        dto.setEmail(doctor.getUser().getEmail());
        dto.setPhoneNumber(doctor.getUser().getPhoneNumber());

        // --- MAP THÊM 2 TRƯỜNG NÀY TỪ USER ---
        dto.setGender(doctor.getUser().getGender());
        dto.setBirthday(doctor.getUser().getBirthday());
        // -------------------------------------

        dto.setDescription(doctor.getDescription());
        dto.setAcademicDegree(doctor.getAcademicDegree());
        dto.setPrice(doctor.getPrice());

        // Ảnh
        dto.setImage(doctor.getImage());
        dto.setOtherImages(doctor.getOtherImages());

        // Ánh xạ thông tin chuyên khoa
        if (doctor.getSpecialty() != null) {
            SpecialtyResponse specialtyDto = new SpecialtyResponse();
            specialtyDto.setId(doctor.getSpecialty().getId());
            specialtyDto.setName(doctor.getSpecialty().getName());
            specialtyDto.setDescription(doctor.getSpecialty().getDescription());
            specialtyDto.setImageUrls(doctor.getSpecialty().getImageUrls());
            dto.setSpecialty(specialtyDto);
        }

        // Ánh xạ thông tin phòng khám
        if (doctor.getClinic() != null) {
            ClinicResponse clinicDto = new ClinicResponse();
            clinicDto.setId(doctor.getClinic().getId());
            clinicDto.setName(doctor.getClinic().getName());
            clinicDto.setAddress(doctor.getClinic().getAddress());
            clinicDto.setPhoneNumber(doctor.getClinic().getPhoneNumber());
            clinicDto.setDescription(doctor.getClinic().getDescription());
            clinicDto.setImageUrls(doctor.getClinic().getImageUrls());
            dto.setClinic(clinicDto);
        }

        return dto;
    }
}