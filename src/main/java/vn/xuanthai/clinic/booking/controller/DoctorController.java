package vn.xuanthai.clinic.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.xuanthai.clinic.booking.dto.request.DoctorCreateRequest;
import vn.xuanthai.clinic.booking.dto.request.DoctorRegistrationRequest;
import vn.xuanthai.clinic.booking.dto.response.DoctorResponse;
import vn.xuanthai.clinic.booking.service.IDoctorService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DoctorController {

    private final IDoctorService doctorService;

    // ----- API QUẢN TRỊ (CHO ADMIN) -----

    @PostMapping("/admin/doctors")
    @PreAuthorize("hasAnyAuthority('DOCTOR_CREATE')")
    public ResponseEntity<DoctorResponse> createDoctorProfile(@Valid @RequestBody DoctorCreateRequest request) {
        DoctorResponse createdDoctor = doctorService.createDoctorProfile(request);
        return new ResponseEntity<>(createdDoctor, HttpStatus.CREATED);
    }

    // --- API MỚI: ĐĂNG KÝ BÁC SĨ TRỌN GÓI ---
    @PostMapping("/admin/doctors/register")
    @PreAuthorize("hasAnyAuthority('DOCTOR_CREATE')")
    public ResponseEntity<DoctorResponse> registerDoctor(@Valid @RequestBody DoctorRegistrationRequest request) {
        DoctorResponse newDoctor = doctorService.registerDoctor(request);
        return new ResponseEntity<>(newDoctor, HttpStatus.CREATED);
    }

    @PutMapping("/admin/doctors/{id}")
    @PreAuthorize("hasAnyAuthority('DOCTOR_UPDATE')")
    public ResponseEntity<DoctorResponse> updateDoctorProfile(
            @PathVariable Long id,
            @Valid @RequestBody DoctorCreateRequest request) {

        return ResponseEntity.ok(doctorService.updateDoctorProfile(id, request));
    }

    @DeleteMapping("/admin/doctors/{id}")
    @PreAuthorize("hasAnyAuthority('DOCTOR_DELETE')")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }

    // ----- CÁC API CÔNG KHAI (CHO BỆNH NHÂN) -----

    @GetMapping("/public/doctors")
    public ResponseEntity<List<DoctorResponse>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/public/doctors/{id}")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @GetMapping("/public/specialties/{id}/doctors")
    public ResponseEntity<List<DoctorResponse>> getDoctorsBySpecialty(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.findDoctorsBySpecialty(id));
    }

    // ----- CÁC API CHO BÁC SĨ -----
    @GetMapping("/doctors/profile/me")
    @PreAuthorize("hasAuthority('DOCTOR_MANAGE_SCHEDULE')") // Chỉ Bác sĩ mới gọi được
    public ResponseEntity<DoctorResponse> getMyDoctorProfile() {
        // Gọi service xử lý toàn bộ logic
        return ResponseEntity.ok(doctorService.getMyDoctorProfile());
    }
}