package vn.xuanthai.clinic.booking.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.xuanthai.clinic.booking.dto.request.DoctorCreateRequest;
import vn.xuanthai.clinic.booking.dto.request.DoctorRegistrationRequest;
import vn.xuanthai.clinic.booking.dto.request.DoctorSelfUpdateRequest;
import vn.xuanthai.clinic.booking.dto.response.ClinicResponse;
import vn.xuanthai.clinic.booking.dto.response.DoctorResponse;
import vn.xuanthai.clinic.booking.dto.response.SpecialtyResponse;
import vn.xuanthai.clinic.booking.entity.*;
import vn.xuanthai.clinic.booking.exception.BadRequestException;
import vn.xuanthai.clinic.booking.exception.ResourceNotFoundException;
import vn.xuanthai.clinic.booking.repository.*;
import vn.xuanthai.clinic.booking.service.IDoctorService;
import vn.xuanthai.clinic.booking.service.IFileService;
import vn.xuanthai.clinic.booking.service.impl.UserContextService;

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
    private final RoleRepository roleRepository; // Cần thêm cái này để tìm ROLE_DOCTOR
    private final PasswordEncoder passwordEncoder; // Cần thêm cái này để mã hóa mật khẩu
    private final UserContextService userContextService;

    @Override
    @Transactional
    public DoctorResponse createDoctorProfile(DoctorCreateRequest request) {
        // 1. Tìm User
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy User với ID: " + request.getUserId()));

        // 2. Kiểm tra trùng lặp
        if (doctorRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new BadRequestException("Người dùng này đã có hồ sơ bác sĩ rồi. Vui lòng dùng chức năng Sửa.");
        }

        // 3. --- LOGIC PHÂN QUYỀN PHÒNG KHÁM (MỚI) ---
        // Nếu người tạo là Clinic Admin -> Ép buộc bác sĩ mới phải thuộc phòng khám đó
        Long currentClinicId = userContextService.getCurrentClinicId();
        if (currentClinicId != null) {
            request.setClinicId(currentClinicId);
        }
        // --------------------------------------------

        // 4. Cập nhật thông tin cá nhân vào User
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getBirthday() != null) user.setBirthday(request.getBirthday());

        user.setClinicId(request.getClinicId());

        // 5. Tự động cấp quyền ROLE_DOCTOR
        boolean hasDoctorRole = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_DOCTOR"));

        if (!hasDoctorRole) {
            Role doctorRole = roleRepository.findByName("ROLE_DOCTOR")
                    .orElseThrow(() -> new ResourceNotFoundException("Lỗi hệ thống: Không tìm thấy ROLE_DOCTOR"));
            user.getRoles().add(doctorRole);
        }

        // 6. Tìm Chuyên khoa và Phòng khám (Dùng ID đã được xử lý ở bước 3)
        Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Chuyên khoa"));

        Clinic clinic = clinicRepository.findById(request.getClinicId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Phòng khám"));

        // 7. Tạo Entity Doctor mới
        Doctor newDoctor = new Doctor();
        newDoctor.setUser(user);
        newDoctor.setSpecialty(specialty);
        newDoctor.setClinic(clinic);
        newDoctor.setDescription(request.getDescription());
        newDoctor.setAcademicDegree(request.getAcademicDegree());
        newDoctor.setPrice(request.getPrice());
        newDoctor.setImage(request.getImage());
        newDoctor.setOtherImages(request.getOtherImages());

        // 8. Lưu
        Doctor savedDoctor = doctorRepository.save(newDoctor);
        return mapToDoctorResponse(savedDoctor);
    }

    @Override
    @Transactional
    public DoctorResponse registerDoctor(DoctorRegistrationRequest request) {

        // 0. --- LOGIC PHÂN QUYỀN PHÒNG KHÁM (MỚI) ---
        // Xử lý ngay từ đầu để đảm bảo tính nhất quán
        Long currentClinicId = userContextService.getCurrentClinicId();
        if (currentClinicId != null) {
            request.setClinicId(currentClinicId);
        }
        // --------------------------------------------

        // --- BƯỚC 1: TẠO TÀI KHOẢN USER ---

        // 1.1. Kiểm tra email trùng
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email đã tồn tại trong hệ thống.");
        }

        // 1.2. Tạo User Entity
        User newUser = new User();
        newUser.setFullName(request.getFullName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setPhoneNumber(request.getPhoneNumber());
        newUser.setAddress(request.getAddress());
        newUser.setGender(request.getGender());
        newUser.setBirthday(request.getBirthday());
        newUser.setClinicId(request.getClinicId());
        newUser.setActive(true);

        // 1.3. Gán quyền Bác sĩ
        Role doctorRole = roleRepository.findByName("ROLE_DOCTOR")
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò DOCTOR."));
        newUser.setRoles(java.util.Set.of(doctorRole));

        // 1.4. Lưu User
        User savedUser = userRepository.save(newUser);

        // --- BƯỚC 2: TẠO HỒ SƠ BÁC SĨ ---

        // 2.1. Tìm Chuyên khoa và Phòng khám (Dùng ID đã xử lý ở bước 0)
        Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Chuyên khoa"));
        Clinic clinic = clinicRepository.findById(request.getClinicId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Phòng khám"));

        // 2.2. Tạo Doctor Entity
        Doctor newDoctor = new Doctor();
        newDoctor.setUser(savedUser);
        newDoctor.setSpecialty(specialty);
        newDoctor.setClinic(clinic);
        newDoctor.setDescription(request.getDescription());
        newDoctor.setAcademicDegree(request.getAcademicDegree());
        newDoctor.setPrice(request.getPrice());
        newDoctor.setImage(request.getImage());
        newDoctor.setOtherImages(request.getOtherImages());

        // 2.3. Lưu Doctor
        Doctor savedDoctor = doctorRepository.save(newDoctor);

        // --- BƯỚC 3: TRẢ VỀ KẾT QUẢ ---
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
        // 1. Kiểm tra xem người gọi có phải là Clinic Admin không
        Long currentClinicId = userContextService.getCurrentClinicId();

        List<Doctor> doctors;

        if (currentClinicId != null) {
            // TRƯỜNG HỢP: ADMIN CHI NHÁNH
            // Chỉ lấy bác sĩ thuộc phòng khám đó
            doctors = doctorRepository.findAllByClinicId(currentClinicId);
        } else {
            // TRƯỜNG HỢP: SUPER ADMIN (hoặc khách vãng lai không đăng nhập)
            // Lấy tất cả
            doctors = doctorRepository.findAll();
        }

        // 2. Chuyển đổi sang DTO
        return doctors.stream()
                .map(this::mapToDoctorResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorResponse> getAllPublicDoctors() {
        // Đơn giản là lấy tất cả từ DB
        return doctorRepository.findAll().stream()
                .map(this::mapToDoctorResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DoctorResponse getMyDoctorProfile() {
        // 1. Lấy email của người đang đăng nhập từ SecurityContext
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // 1. In log ra để debug xem ai đang đăng nhập
        System.out.println("DEBUG: Đang tìm profile cho email: " + currentEmail);

        // 2. Tìm User theo email
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng."));

        // 3. Tìm Doctor theo User ID
        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bạn chưa có hồ sơ bác sĩ. Vui lòng liên hệ Admin."));

        // 4. Ánh xạ sang DTO và trả về
        return mapToDoctorResponse(doctor);
    }

    @Override
    @Transactional
    public DoctorResponse updateMyDoctorProfile(DoctorSelfUpdateRequest request) {
        // 1. Lấy bác sĩ đang đăng nhập
        User currentUser = userContextService.getCurrentUser();
        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Hồ sơ bác sĩ không tồn tại"));

        // 2. Chỉ cập nhật các trường cho phép
        if (request.getDescription() != null) doctor.setDescription(request.getDescription());
        if (request.getAcademicDegree() != null) doctor.setAcademicDegree(request.getAcademicDegree());
        if (request.getImage() != null) doctor.setImage(request.getImage());
        if (request.getOtherImages() != null) doctor.setOtherImages(request.getOtherImages());

        return mapToDoctorResponse(doctorRepository.save(doctor));
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
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
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
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        // 1. Lưu lại User ID trước khi xóa Doctor
        Long userId = doctor.getUser().getId();

        // 2. Xóa ảnh trên Cloudinary (Code cũ)
        if (doctor.getImage() != null) fileService.deleteFile(doctor.getImage());
        if (doctor.getOtherImages() != null) {
            for (String url : doctor.getOtherImages()) fileService.deleteFile(url);
        }

        // 3. Xóa Hồ sơ Bác sĩ trước (Vì Doctor tham chiếu đến User)
        doctorRepository.delete(doctor);

        // 4. Xóa luôn Tài khoản User
        userRepository.deleteById(userId);
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
        dto.setAddress(doctor.getUser().getAddress());

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