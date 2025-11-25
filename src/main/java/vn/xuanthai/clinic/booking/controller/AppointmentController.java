package vn.xuanthai.clinic.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.xuanthai.clinic.booking.dto.request.AppointmentRequest;
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

    @GetMapping("/appointments/my-history")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointmentHistory(Authentication authentication) {
        return ResponseEntity.ok(appointmentService.getMyAppointments(authentication));
    }

    // (Em có thể tự viết API cho phép Bệnh nhân hủy lịch)
    // @PutMapping("/appointments/{id}/cancel")
    // @PreAuthorize("hasRole('PATIENT')")

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