package vn.xuanthai.clinic.booking.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.xuanthai.clinic.booking.dto.request.AppointmentRequest;
import vn.xuanthai.clinic.booking.dto.request.CompletionRequest;
import vn.xuanthai.clinic.booking.dto.response.AppointmentResponse;
import vn.xuanthai.clinic.booking.entity.Appointment;
import vn.xuanthai.clinic.booking.entity.Doctor;
import vn.xuanthai.clinic.booking.entity.Schedule;
import vn.xuanthai.clinic.booking.entity.User;
import vn.xuanthai.clinic.booking.enums.AppointmentStatus;
import vn.xuanthai.clinic.booking.enums.ScheduleStatus;
import vn.xuanthai.clinic.booking.exception.BadRequestException;
import vn.xuanthai.clinic.booking.exception.ResourceNotFoundException;
import vn.xuanthai.clinic.booking.repository.AppointmentRepository;
import vn.xuanthai.clinic.booking.repository.DoctorRepository;
import vn.xuanthai.clinic.booking.repository.ScheduleRepository;
import vn.xuanthai.clinic.booking.repository.UserRepository;
import vn.xuanthai.clinic.booking.service.IAppointmentService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements IAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final UserContextService userContextService;
    private final DoctorRepository doctorRepository;

    @Override
    @Transactional // Rất quan trọng! Đảm bảo CẢ HAI thao tác cùng thành công
    public AppointmentResponse createAppointment(AppointmentRequest request, Authentication authentication) {

        // 1. Lấy thông tin bệnh nhân đang đăng nhập
        String currentUsername = authentication.getName(); // Lấy email
        User patient = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản bệnh nhân."));

        // 2. Tìm khung giờ (Schedule) mà bệnh nhân muốn đặt
        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khung giờ này."));

        // 3. KIỂM TRA NGHIỆP VỤ QUAN TRỌNG NHẤT (Chống double-booking)
        if (schedule.getStatus() == ScheduleStatus.BOOKED) {
            throw new BadRequestException("Khung giờ này đã có người khác đặt. Vui lòng chọn giờ khác.");
        }

        // 4. CẬP NHẬT TRẠNG THÁI KHUNG GIỜ
        // "Khóa" khung giờ này lại
        schedule.setStatus(ScheduleStatus.BOOKED);
        scheduleRepository.save(schedule);

        // 5. TẠO LỊCH HẸN MỚI
        Appointment newAppointment = new Appointment();
        newAppointment.setSchedule(schedule);
        newAppointment.setPatient(patient);
        newAppointment.setReason(request.getReason());
        newAppointment.setStatus(AppointmentStatus.CONFIRMED); // Xác nhận luôn

        Appointment savedAppointment = appointmentRepository.save(newAppointment);

        // (TODO: Gửi message vào Message Queue để thông báo cho bác sĩ/bệnh nhân)

        // 6. Trả về DTO
        return mapToResponse(savedAppointment);
    }

    @Override
    public List<AppointmentResponse> getMyAppointments() {
        // 1. Dùng trợ lý UserContextService để lấy người đang đăng nhập
        User currentUser = userContextService.getCurrentUser();

        // 2. Gọi Repository tìm lịch hẹn theo ID bệnh nhân
        List<Appointment> appointments = appointmentRepository.findAllByPatientId(currentUser.getId());

        // 3. Map sang DTO
        return appointments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ... Inject DoctorRepository vào

    @Override
    public List<AppointmentResponse> getAllAppointmentsForDoctor() {
        // 1. Lấy User hiện tại
        User currentUser = userContextService.getCurrentUser();

        // 2. Tìm hồ sơ Bác sĩ của User này
        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bạn không phải là bác sĩ!"));

        // 3. Lấy danh sách lịch hẹn của bác sĩ này
        List<Appointment> appointments = appointmentRepository.findAllBySchedule_Doctor_Id(doctor.getId());

        return appointments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentResponse> getAllAppointments() {
        // 1. Kiểm tra xem người gọi có phải là Admin Chi Nhánh không
        Long currentClinicId = userContextService.getCurrentClinicId();

        List<Appointment> appointments;

        if (currentClinicId != null) {
            // TRƯỜNG HỢP: ADMIN CHI NHÁNH
            // Chỉ lấy lịch hẹn thuộc phòng khám của họ
            appointments = appointmentRepository.findAllBySchedule_Doctor_Clinic_Id(currentClinicId);
        } else {
            // TRƯỜNG HỢP: SUPER ADMIN
            // Lấy tất cả
            appointments = appointmentRepository.findAll();
        }

        return appointments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateStatus(Long appointmentId, AppointmentStatus newStatus) {
        // 1. Tìm lịch hẹn
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn: " + appointmentId));

        // 2. BẢO MẬT: Kiểm tra quyền Clinic Admin (Chặn sửa chéo phòng khám)
        Long currentClinicId = userContextService.getCurrentClinicId();
        if (currentClinicId != null) {
            Long appointmentClinicId = appointment.getSchedule().getDoctor().getClinic().getId();
            if (!appointmentClinicId.equals(currentClinicId)) {
                throw new AccessDeniedException("Bạn không có quyền chỉnh sửa lịch hẹn của phòng khám khác!");
            }
        }

        // 3. Xử lý logic theo trạng thái
        if (newStatus == AppointmentStatus.CANCELLED) {
            // Gọi hàm chung để xử lý hủy
            processCancellation(appointment);
        } else {
            // Các trạng thái khác (CONFIRMED, COMPLETED...)
            appointment.setStatus(newStatus);
            appointmentRepository.save(appointment);
        }
    }

    @Override
    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId, Authentication authentication) {
        // 1. Tìm lịch hẹn
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn: " + appointmentId));

        // 2. Lấy User hiện tại
        User currentUser = userContextService.getCurrentUser();

        // 3. KIỂM TRA QUYỀN (Owner hoặc Admin/Staff có quyền Cancel)
        boolean isOwner = appointment.getPatient().getId().equals(currentUser.getId());

        // Kiểm tra quyền dựa trên Authority (thay vì fix cứng Role)
        boolean hasCancelPermission = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("APPOINTMENT_CANCEL") ||
                        a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (!isOwner && !hasCancelPermission) {
            throw new AccessDeniedException("Bạn không có quyền hủy lịch hẹn này.");
        }

        // 4. Gọi hàm chung để xử lý hủy
        processCancellation(appointment);

        return mapToResponse(appointment);
    }

    // --- HÀM TRỢ GIÚP (PRIVATE) ---
    // Logic hủy lịch được gom vào đây để tái sử dụng
    private void processCancellation(Appointment appointment) {
        // Validate logic nghiệp vụ
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Lịch hẹn này đã được hủy trước đó.");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Không thể hủy lịch hẹn đã hoàn thành (đã khám xong).");
        }

        // 1. Cập nhật trạng thái lịch hẹn
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        // 2. Trả lại khung giờ (Schedule) thành AVAILABLE
        Schedule schedule = appointment.getSchedule();
        schedule.setStatus(ScheduleStatus.AVAILABLE);
        scheduleRepository.save(schedule);

        // (Tại đây có thể thêm logic gửi email thông báo hủy)
    }

    @Override
    @Transactional
    public void completeAppointment(Long appointmentId, CompletionRequest request) {
        // 1. Tìm lịch hẹn
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn: " + appointmentId));

        // 2. KIỂM TRA QUYỀN (Quan trọng)
        // Chỉ Bác sĩ phụ trách lịch hẹn này (hoặc Admin) mới được phép nhập kết quả
        User currentUser = userContextService.getCurrentUser();

        // Lấy ID của bác sĩ trong lịch hẹn
        Long doctorUserId = appointment.getSchedule().getDoctor().getUser().getId();

        // Kiểm tra: Người đang đăng nhập có phải là bác sĩ của lịch này không?
        // (Hoặc có thể check thêm quyền ROLE_ADMIN nếu muốn cho phép Admin nhập hộ)
        boolean isMyPatient = currentUser.getId().equals(doctorUserId);

        if (!isMyPatient) {
            // Có thể check thêm quyền Admin ở đây nếu cần
            throw new AccessDeniedException("Bạn không phải là bác sĩ phụ trách ca này.");
        }

        // 3. Kiểm tra trạng thái hợp lệ
        // Chỉ được complete khi trạng thái đang là CONFIRMED (Đã xác nhận)
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BadRequestException("Lịch hẹn phải được xác nhận trước khi khám.");
        }

        // 4. Cập nhật thông tin
        appointment.setDiagnosis(request.getDiagnosis());
        appointment.setPrescription(request.getPrescription());

        // 5. Đổi trạng thái thành COMPLETED
        appointment.setStatus(AppointmentStatus.COMPLETED);

        // 6. Lưu vào CSDL
        appointmentRepository.save(appointment);

        // (Có thể gửi email đơn thuốc cho bệnh nhân ở đây)
    }

    // --- Phương thức trợ giúp ---
    private AppointmentResponse mapToResponse(Appointment app) {
        // 1. Khởi tạo DTO rỗng
        AppointmentResponse dto = new AppointmentResponse();

        // 2. Map các thông tin cơ bản của Lịch hẹn
        dto.setId(app.getId());
        dto.setCreatedAt(app.getCreatedAt());
        dto.setStatus(app.getStatus());
        dto.setReason(app.getReason());

        // Map thông tin kết quả khám (nếu có)
        dto.setDiagnosis(app.getDiagnosis());
        dto.setPrescription(app.getPrescription());

        // 3. Map thông tin Bệnh nhân (Patient)
        // Lưu ý: Patient là một User entity
        User patient = app.getPatient();
        if (patient != null) {
            dto.setPatientId(patient.getId());
            dto.setPatientName(patient.getFullName());
            dto.setPatientPhone(patient.getPhoneNumber()); // Thêm SĐT để bác sĩ tiện liên hệ
            // Có thể thêm patientGender, patientBirthday nếu cần hiển thị chi tiết
        }

        // 4. Map thông tin từ Khung giờ (Schedule) -> Bác sĩ -> Phòng khám
        Schedule schedule = app.getSchedule();
        if (schedule != null) {
            // Map ngày giờ khám
            dto.setAppointmentDate(schedule.getDate());
            dto.setAppointmentTimeSlot(schedule.getTimeSlot());

            Doctor doctor = schedule.getDoctor();
            if (doctor != null) {
                // Map thông tin Bác sĩ
                dto.setDoctorId(doctor.getId());

                // Map thông tin User của Bác sĩ (Tên, Email...)
                if (doctor.getUser() != null) {
                    dto.setDoctorName(doctor.getUser().getFullName());
                }

                // Map thông tin Chuyên khoa
                if (doctor.getSpecialty() != null) {
                    dto.setSpecialtyName(doctor.getSpecialty().getName());
                }

                // Map thông tin Phòng khám
                if (doctor.getClinic() != null) {
                    dto.setClinicName(doctor.getClinic().getName());
                    // Có thể thêm clinicAddress nếu cần hiển thị địa chỉ khám
                }
            }
        }

        return dto;
    }
}