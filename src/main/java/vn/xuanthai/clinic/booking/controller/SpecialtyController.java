package vn.xuanthai.clinic.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.xuanthai.clinic.booking.dto.request.SpecialtyRequest;
import vn.xuanthai.clinic.booking.dto.response.SpecialtyResponse;
import vn.xuanthai.clinic.booking.service.ISpecialtyService;

import java.util.List;

@RestController
@RequestMapping("/api") // Đường dẫn gốc chung
@RequiredArgsConstructor
public class SpecialtyController {

    private final ISpecialtyService specialtyService;

    // ----- CÁC API CÔNG KHAI (CHO BỆNH NHÂN) -----
    // Bệnh nhân cần xem danh sách chuyên khoa để tìm kiếm

    @GetMapping("/public/specialties")
    public ResponseEntity<List<SpecialtyResponse>> getAllSpecialties() {
        return ResponseEntity.ok(specialtyService.getAllSpecialties());
    }

    @GetMapping("/public/specialties/{id}")
    public ResponseEntity<SpecialtyResponse> getSpecialtyById(@PathVariable Long id) {
        return ResponseEntity.ok(specialtyService.getSpecialtyById(id));
    }

    // ----- CÁC API QUẢN TRỊ (CHO ADMIN) -----

    @PostMapping("/admin/specialties")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<SpecialtyResponse> createSpecialty(@Valid @RequestBody SpecialtyRequest request) {
        SpecialtyResponse createdSpecialty = specialtyService.createSpecialty(request);
        return new ResponseEntity<>(createdSpecialty, HttpStatus.CREATED); // Trả về 201 Created
    }

    @PutMapping("/admin/specialties/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<SpecialtyResponse> updateSpecialty(@PathVariable Long id,
                                                             @Valid @RequestBody SpecialtyRequest request) {
        return ResponseEntity.ok(specialtyService.updateSpecialty(id, request));
    }

    @DeleteMapping("/admin/specialties/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<Void> deleteSpecialty(@PathVariable Long id) {
        specialtyService.deleteSpecialty(id);
        return ResponseEntity.noContent().build(); // Trả về 204 No Content
    }
}