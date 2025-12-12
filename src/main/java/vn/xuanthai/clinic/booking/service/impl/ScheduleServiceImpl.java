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
import vn.xuanthai.clinic.booking.exception.BadRequestException;
import vn.xuanthai.clinic.booking.exception.ResourceNotFoundException;
import vn.xuanthai.clinic.booking.repository.DoctorRepository;
import vn.xuanthai.clinic.booking.repository.ScheduleRepository;
import vn.xuanthai.clinic.booking.repository.UserRepository;
import vn.xuanthai.clinic.booking.service.IScheduleService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements IScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final UserContextService userContextService;

    @Override
    @Transactional
    public List<ScheduleResponse> createDoctorSchedule(ScheduleCreateRequest request, Authentication authentication) {

        // 1. Tìm bác sĩ & Kiểm tra quyền (GIỮ NGUYÊN CODE CỦA EM)
        Doctor targetDoctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bác sĩ"));

        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
                authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        boolean isOwner = targetDoctor.getUser().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Không có quyền tạo lịch.");
        }

        // 2. CHECK 1: Không được tạo lịch cho QUÁ KHỨ (Ngày cũ)
        if (request.getDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Không thể tạo lịch cho ngày trong quá khứ!");
        }

        // 3. Lấy danh sách các khung giờ ĐÃ TỒN TẠI của bác sĩ trong ngày đó
        // (Để tránh tạo trùng lặp)
        List<Schedule> existingSchedules = scheduleRepository.findByDoctorIdAndDateOrderByTimeSlotAsc(
                targetDoctor.getId(),
                request.getDate()
        );

        // Tạo một Set chứa các timeSlot đã có để so sánh cho nhanh
        Set<String> existingSlots = existingSchedules.stream()
                .map(Schedule::getTimeSlot)
                .collect(Collectors.toSet());

        List<Schedule> newSchedules = new ArrayList<>();

        // Lấy giờ hiện tại để check (nếu tạo lịch cho ngày hôm nay)
        LocalTime now = LocalTime.now();
        boolean isToday = request.getDate().equals(LocalDate.now());

        for (String timeSlot : request.getTimeSlots()) {

            // 4. CHECK 2: Tránh trùng lặp (Duplicate)
            if (existingSlots.contains(timeSlot)) {
                // Nếu giờ này đã có trong DB rồi -> Bỏ qua, không tạo lại
                continue;
            }

            // 5. CHECK 3: Không được tạo giờ đã qua (nếu là hôm nay)
            // Cần parse string "08:00" ra LocalTime để so sánh
            if (isToday) {
                try {
                    // Lấy phần giờ bắt đầu "08:00" từ chuỗi "08:00 - 08:30" nếu cần
                    // Ở đây giả sử timeSlot lưu là "08:00" hoặc em parse như Frontend
                    String startTimeStr = timeSlot.split(" - ")[0]; // Lấy giờ bắt đầu
                    LocalTime slotTime = LocalTime.parse(startTimeStr);

                    if (slotTime.isBefore(now)) {
                        continue; // Giờ đã qua -> Bỏ qua
                    }
                } catch (Exception e) {
                    // Log lỗi parse nếu cần
                    continue;
                }
            }

            // Nếu vượt qua mọi bài test -> Tạo mới
            Schedule schedule = new Schedule();
            schedule.setDoctor(targetDoctor);
            schedule.setDate(request.getDate());
            schedule.setTimeSlot(timeSlot);
            schedule.setStatus(ScheduleStatus.AVAILABLE);
            newSchedules.add(schedule);
        }

        // Nếu không có lịch nào hợp lệ (do trùng hết hoặc quá khứ hết)
        if (newSchedules.isEmpty()) {
            // Có thể return list rỗng hoặc ném lỗi tùy em
            // throw new BadRequestException("Các khung giờ chọn đều không hợp lệ hoặc đã tồn tại.");
            return new ArrayList<>();
        }

        // ---------------------------------------------

        List<Schedule> savedSchedules = scheduleRepository.saveAll(newSchedules);

        return savedSchedules.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleResponse> getSchedulesByDoctorAndDate(Long doctorId, LocalDate date) {
        // Gọi hàm có sắp xếp
        return scheduleRepository.findByDoctorIdAndDateOrderByTimeSlotAsc(doctorId, date)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleResponse> getAvailableSchedulesByDoctorAndDate(Long doctorId, LocalDate date) {
        return scheduleRepository.findByDoctorIdAndDateAndStatus(doctorId, date, ScheduleStatus.AVAILABLE).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getWorkingDays(int year, int month) {
        User currentUser = userContextService.getCurrentUser();
        Doctor doctor = doctorRepository.findByUserId(currentUser.getId()).orElseThrow();

        // 1. Tính ngày đầu tháng và cuối tháng
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 2. Gọi Repo lấy danh sách ngày
        List<LocalDate> dates = scheduleRepository.findDistinctDatesByDoctorIdAndDateBetween(doctor.getId(), startDate, endDate);

        // 3. Chuyển sang String "YYYY-MM-DD" để Frontend dễ so sánh
        return dates.stream()
                .map(LocalDate::toString)
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