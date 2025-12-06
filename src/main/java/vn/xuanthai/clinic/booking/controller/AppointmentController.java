package vn.xuanthai.clinic.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.xuanthai.clinic.booking.dto.request.AppointmentRequest;
import vn.xuanthai.clinic.booking.dto.request.BookingRequest;
import vn.xuanthai.clinic.booking.dto.request.CompletionRequest;
import vn.xuanthai.clinic.booking.dto.response.AppointmentResponse;
import vn.xuanthai.clinic.booking.enums.AppointmentStatus;
import vn.xuanthai.clinic.booking.service.IAppointmentService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AppointmentController {

    private final IAppointmentService appointmentService;

    // ----- API DÀNH CHO BỆNH NHÂN (Đã xác thực) -----

    @PostMapping("/appointments")
    @PreAuthorize("hasRole('PATIENT')") // Chỉ Bệnh nhân mới được đặt lịch
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody AppointmentRequest request,
            Authentication authentication) { // Lấy thông tin người đặt

        AppointmentResponse createdAppointment = appointmentService.createAppointment(request, authentication);
        return new ResponseEntity<>(createdAppointment, HttpStatus.CREATED);
    }

    // 1. API Cho Bệnh Nhân (Xem lịch sử khám của mình)
    @GetMapping("/appointments/my-history")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointmentHistory() {
        // Gọi hàm tìm theo PatientId
        return ResponseEntity.ok(appointmentService.getMyAppointments());
    }

    // 2. API Cho Bác Sĩ (Xem danh sách bệnh nhân đăng ký khám mình)
    @GetMapping("/doctor/appointments")
    @PreAuthorize("hasAuthority('DOCTOR_MANAGE_SCHEDULE')")
    public ResponseEntity<List<AppointmentResponse>> getDoctorAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointmentsForDoctor());
    }

    // API Dành cho Bác sĩ: Hoàn tất khám bệnh
    @PutMapping("/doctor/appointments/{id}/complete")
    @PreAuthorize("hasAuthority('DOCTOR_MANAGE_SCHEDULE')") // Yêu cầu quyền bác sĩ
    public ResponseEntity<Void> completeAppointment(
            @PathVariable Long id,
            @Valid @RequestBody CompletionRequest request) { // Dùng DTO và @Valid

        appointmentService.completeAppointment(id, request);
        return ResponseEntity.ok().build();
    }

    // API Đặt lịch (Dành cho Bệnh nhân)
    @PostMapping("/appointments/book")
    @PreAuthorize("hasRole('PATIENT')") // Chỉ bệnh nhân mới được đặt
    public ResponseEntity<AppointmentResponse> bookAppointment(@RequestBody BookingRequest request) {
        return ResponseEntity.ok(appointmentService.bookAppointment(request));
    }

    // API Hủy lịch (Dành cho Bệnh nhân tự hủy lịch của mình)
    @PutMapping("/appointments/{id}/cancel")
    @PreAuthorize("hasRole('PATIENT')") // Chỉ cho phép bệnh nhân
    public ResponseEntity<AppointmentResponse> cancelMyAppointment(
            @PathVariable Long id,
            Authentication authentication) {

        // Gọi hàm service (hàm này em đã viết ở các bài trước)
        return ResponseEntity.ok(appointmentService.cancelAppointment(id, authentication));
    }

    // ----- API DÀNH CHO ADMIN -----

    @GetMapping("/admin/appointments")
    @PreAuthorize("hasAnyAuthority('APPOINTMENT_VIEW')")
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @PutMapping("/admin/appointments/{id}/status")
    @PreAuthorize("hasAuthority('APPOINTMENT_APPROVE') or hasAuthority('APPOINTMENT_CANCEL')")
    public ResponseEntity<Void> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status) { // Nhận status từ param url?status=...

        appointmentService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }
}