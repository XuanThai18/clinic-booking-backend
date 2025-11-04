package vn.xuanthai.clinic.booking.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.xuanthai.clinic.booking.dto.request.AppointmentRequest;
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
    public List<AppointmentResponse> getMyAppointments(Authentication authentication) {
        String currentUsername = authentication.getName();
        User patient = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản."));

        // Cần thêm phương thức này vào AppointmentRepository
        return appointmentRepository.findByPatientId(patient.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Hủy một lịch hẹn.
     * Chỉ có chủ nhân của lịch hẹn (Patient) hoặc Admin/SuperAdmin mới có quyền này.
     * Khi hủy, lịch hẹn sẽ bị CANCELED và khung giờ (Schedule) sẽ được trả về AVAILABLE.
     */
    @Override
    @Transactional // Rất quan trọng! Đảm bảo cả 2 bảng được cập nhật cùng nhau
    public AppointmentResponse cancelAppointment(Long appointmentId, Authentication authentication) {

        // 1. Tìm lịch hẹn (Appointment) cần hủy
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn với ID: " + appointmentId));

        // 2. Lấy thông tin người dùng đang đăng nhập
        String currentUsername = authentication.getName(); // Đây là email
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản người dùng."));

        // 3. KIỂM TRA QUYỀN HẠN (Authorization)
        // Kiểm tra xem người này có phải là Admin/SuperAdmin không
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") ||
                        auth.getAuthority().equals("ROLE_SUPER_ADMIN"));

        // Kiểm tra xem người này có phải là chủ nhân (Patient) của lịch hẹn không
        boolean isOwner = appointment.getPatient().getId().equals(currentUser.getId());

        // Nếu không phải Admin VÀ cũng không phải Chủ nhân -> Ném lỗi 403 Forbidden
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Bạn không có quyền hủy lịch hẹn này.");
        }

        // 4. KIỂM TRA NGHIỆP VỤ (Business Logic)
        // Kiểm tra xem lịch hẹn đã bị hủy hoặc đã hoàn thành chưa
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Lịch hẹn này đã được hủy trước đó.");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Không thể hủy lịch hẹn đã hoàn thành.");
        }

        // 5. THỰC THI LOGIC HỦY LỊCH (Transaction)
        // 5.1. Đổi trạng thái lịch hẹn
        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // 5.2. "Mở khóa" lại khung giờ (Schedule)
        Schedule schedule = appointment.getSchedule();
        schedule.setStatus(ScheduleStatus.AVAILABLE);
        scheduleRepository.save(schedule);

        // (TODO: Gửi email thông báo hủy lịch cho bác sĩ và bệnh nhân)

        // 6. Trả về thông tin lịch hẹn đã được cập nhật
        return mapToResponse(savedAppointment);
    }

    // --- Phương thức trợ giúp ---
    private AppointmentResponse mapToResponse(Appointment app) {
        AppointmentResponse dto = new AppointmentResponse();
        dto.setId(app.getId());
        dto.setCreatedAt(app.getCreatedAt());
        dto.setStatus(app.getStatus());
        dto.setReason(app.getReason());

        // Lấy thông tin Bệnh nhân
        dto.setPatientId(app.getPatient().getId());
        dto.setPatientName(app.getPatient().getFullName());

        // Lấy thông tin từ Schedule (là nơi chứa thông tin Doctor và Clinic)
        Schedule schedule = app.getSchedule();
        dto.setAppointmentDate(schedule.getDate());
        dto.setAppointmentTimeSlot(schedule.getTimeSlot());

        Doctor doctor = schedule.getDoctor();
        dto.setDoctorId(doctor.getId());
        dto.setDoctorName(doctor.getUser().getFullName());
        dto.setClinicName(doctor.getClinic().getName());
        dto.setSpecialtyName(doctor.getSpecialty().getName());

        return dto;
    }
}