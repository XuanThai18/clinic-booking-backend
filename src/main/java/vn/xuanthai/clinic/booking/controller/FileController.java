package vn.xuanthai.clinic.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.xuanthai.clinic.booking.service.IFileService; // Đảm bảo import đúng interface của em

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/files")
@RequiredArgsConstructor
public class FileController {

    private final IFileService fileService;

    @PostMapping("/upload-multiple") // Đổi tên endpoint cho rõ nghĩa
    @PreAuthorize("hasAnyAuthority('FILE_UPLOAD')")
    public ResponseEntity<Map<String, ?>> uploadFiles(
            @RequestParam("files") MultipartFile[] files
    ) {

        if (files.length == 0) {
            return ResponseEntity.badRequest().body(Map.of("error", List.of("Vui lòng chọn ít nhất một file.")));
        }

        List<String> fileUrls = fileService.uploadFiles(files);

        // Trả về một JSON chứa danh sách các URL
        Map<String, List<String>> response = Map.of("urls", fileUrls);

        return ResponseEntity.ok(response);
    }
}