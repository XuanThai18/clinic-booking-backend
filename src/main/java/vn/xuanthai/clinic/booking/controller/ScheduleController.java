package vn.xuanthai.clinic.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.xuanthai.clinic.booking.dto.request.ScheduleCreateRequest;
import vn.xuanthai.clinic.booking.dto.response.ScheduleResponse;
import vn.xuanthai.clinic.booking.service.IScheduleService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScheduleController {

    private final IScheduleService scheduleService;

    // ----- API DÀNH CHO BÁC SĨ / ADMIN -----

    @PostMapping("/schedules")
    @PreAuthorize("hasAnyAuthority('DOCTOR_MANAGE_SCHEDULE')")
    public ResponseEntity<List<ScheduleResponse>> createDoctorSchedule(
            @Valid @RequestBody ScheduleCreateRequest request,
            Authentication authentication) { // Spring sẽ tự động "tiêm" thông tin người dùng đang đăng nhập vào đây

        List<ScheduleResponse> schedules = scheduleService.createDoctorSchedule(request, authentication);
        return new ResponseEntity<>(schedules, HttpStatus.CREATED);
    }

    // API cho Admin xem lịch chi tiết của một bác sĩ
    @GetMapping("/admin/doctors/{doctorId}/schedules")
    @PreAuthorize("hasAnyAuthority('DOCTOR_MANAGE_SCHEDULE')")
    public ResponseEntity<List<ScheduleResponse>> getFullScheduleForAdmin(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return ResponseEntity.ok(scheduleService.getSchedulesByDoctorAndDate(doctorId, date));
    }

    // API lấy ngày bác sĩ có lịch làm việc (hiển thị dấu chấm vào ngày đó để tối ưu người dùng)
    @GetMapping("/doctor/schedules/working-days")
    @PreAuthorize("hasAuthority('DOCTOR_MANAGE_SCHEDULE')")
    public ResponseEntity<List<String>> getWorkingDays(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(scheduleService.getWorkingDays(year, month));
    }


    // ----- API CÔNG KHAI (CHO BỆNH NHÂN) -----

    @GetMapping("/public/schedules")
    public ResponseEntity<List<ScheduleResponse>> getSchedulesForPatient(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // Gọi Service lấy lịch (nhớ dùng hàm có sắp xếp OrderByTime...)
        return ResponseEntity.ok(scheduleService.getSchedulesByDoctorAndDate(doctorId, date));
    }
}