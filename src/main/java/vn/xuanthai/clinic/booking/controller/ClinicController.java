package vn.xuanthai.clinic.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.xuanthai.clinic.booking.dto.request.ClinicRequest;
import vn.xuanthai.clinic.booking.dto.response.ClinicResponse;
import vn.xuanthai.clinic.booking.service.IClinicService;

import java.util.List;

@RestController
@RequestMapping("/api") // Đường dẫn gốc chung
@RequiredArgsConstructor
public class ClinicController {

    private final IClinicService clinicService;

    // ----- CÁC API CÔNG KHAI (CHO BỆNH NHÂN) -----

    @GetMapping("/public/clinics")
    public ResponseEntity<List<ClinicResponse>> getAllClinics() {
        return ResponseEntity.ok(clinicService.getAllClinics());
    }

    @GetMapping("/public/clinics/{id}")
    public ResponseEntity<ClinicResponse> getClinicById(@PathVariable Long id) {
        return ResponseEntity.ok(clinicService.getClinicById(id));
    }

    // ----- CÁC API QUẢN TRỊ (CHO ADMIN) -----

    @PostMapping("/admin/clinics")
    @PreAuthorize("hasAnyAuthority('CLINIC_CREATE')")
    public ResponseEntity<ClinicResponse> createClinic(@Valid @RequestBody ClinicRequest request) {
        ClinicResponse createdClinic = clinicService.createClinic(request);
        return new ResponseEntity<>(createdClinic, HttpStatus.CREATED);
    }

    @PutMapping("/admin/clinics/{id}")
    @PreAuthorize("hasAnyAuthority('CLINIC_UPDATE')")
    public ResponseEntity<ClinicResponse> updateClinic(@PathVariable Long id,
                                                       @Valid @RequestBody ClinicRequest request) {
        return ResponseEntity.ok(clinicService.updateClinic(id, request));
    }

    @DeleteMapping("/admin/clinics/{id}")
    @PreAuthorize("hasAnyAuthority('CLINIC_DELETE')")
    public ResponseEntity<Void> deleteClinic(@PathVariable Long id) {
        clinicService.deleteClinic(id);
        return ResponseEntity.noContent().build();
    }
}