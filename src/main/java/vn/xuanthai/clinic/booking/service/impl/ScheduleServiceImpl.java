package vn.xuanthai.clinic.booking.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.xuanthai.clinic.booking.dto.request.ScheduleCreateRequest;
import vn.xuanthai.clinic.booking.dto.response.ScheduleResponse;
import vn.xuanthai.clinic.booking.entity.Doctor;
import vn.xuanthai.clinic.booking.entity.Schedule;
import vn.xuanthai.clinic.booking.entity.User;
import vn.xuanthai.clinic.booking.enums.ScheduleStatus;
import vn.xuanthai.clinic.booking.exception.ResourceNotFoundException;
import vn.xuanthai.clinic.booking.repository.DoctorRepository;
import vn.xuanthai.clinic.booking.repository.ScheduleRepository;
import vn.xuanthai.clinic.booking.repository.UserRepository;
import vn.xuanthai.clinic.booking.service.IScheduleService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements IScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional // Đảm bảo tất cả các slot được tạo thành công, hoặc rollback
    public List<ScheduleResponse> createDoctorSchedule(ScheduleCreateRequest request, Authentication authentication) {

        // 1. Tìm bác sĩ (target) mà request muốn tạo lịch cho
        Doctor targetDoctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bác sĩ với ID: " + request.getDoctorId()));

        // --- ĐÂY CHÍNH LÀ LOGIC KIỂM TRA QUAN TRỌNG ---
        // Lấy thông tin người dùng đang đăng nhập
        String currentUsername = authentication.getName(); // Đây là email
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng đang đăng nhập."));

        // Kiểm tra xem người đăng nhập có phải là Admin không
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
                authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));

        // Kiểm tra xem người đăng nhập có phải là "chủ sở hữu" của hồ sơ bác sĩ này không
        boolean isOwner = targetDoctor.getUser().getId().equals(currentUser.getId());

        // Nếu người này KHÔNG PHẢI Admin VÀ CŨNG KHÔNG PHẢI chủ sở hữu
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Bạn không có quyền tạo lịch làm việc cho bác sĩ khác.");
        }
        // --- KẾT THÚC KIỂM TRA ---

        // 2. Logic tạo lịch (giữ nguyên như cũ)
        List<Schedule> newSchedules = new ArrayList<>();
        for (String timeSlot : request.getTimeSlots()) {
            Schedule schedule = new Schedule();
            schedule.setDoctor(targetDoctor);
            schedule.setDate(request.getDate());
            schedule.setTimeSlot(timeSlot);
            schedule.setStatus(ScheduleStatus.AVAILABLE);
            newSchedules.add(schedule);
        }

        List<Schedule> savedSchedules = scheduleRepository.saveAll(newSchedules);

        return savedSchedules.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleResponse> getSchedulesByDoctorAndDate(Long doctorId, LocalDate date) {
        return scheduleRepository.findByDoctorIdAndDate(doctorId, date).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleResponse> getAvailableSchedulesByDoctorAndDate(Long doctorId, LocalDate date) {
        return scheduleRepository.findByDoctorIdAndDateAndStatus(doctorId, date, ScheduleStatus.AVAILABLE).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // --- Phương thức trợ giúp ---
    private ScheduleResponse mapToResponse(Schedule schedule) {
        ScheduleResponse dto = new ScheduleResponse();
        dto.setId(schedule.getId());
        dto.setDoctorId(schedule.getDoctor().getId());
        dto.setDate(schedule.getDate());
        dto.setTimeSlot(schedule.getTimeSlot());
        dto.setStatus(schedule.getStatus());
        return dto;
    }
}