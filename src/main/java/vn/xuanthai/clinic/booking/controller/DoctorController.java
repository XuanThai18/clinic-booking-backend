package vn.xuanthai.clinic.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.xuanthai.clinic.booking.dto.request.DoctorCreateRequest;
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
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<DoctorResponse> createDoctorProfile(@Valid @RequestBody DoctorCreateRequest request) {
        DoctorResponse createdDoctor = doctorService.createDoctorProfile(request);
        return new ResponseEntity<>(createdDoctor, HttpStatus.CREATED);
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
}