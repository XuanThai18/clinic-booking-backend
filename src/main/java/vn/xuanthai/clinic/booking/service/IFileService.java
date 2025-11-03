package vn.xuanthai.clinic.booking.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IFileService {
    List<String> uploadFiles(MultipartFile[] files);
}
