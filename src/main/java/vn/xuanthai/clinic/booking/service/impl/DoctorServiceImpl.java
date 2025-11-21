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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements IDoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final SpecialtyRepository specialtyRepository;
    private final ClinicRepository clinicRepository;

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