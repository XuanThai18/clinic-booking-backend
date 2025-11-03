package vn.xuanthai.clinic.booking.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.xuanthai.clinic.booking.service.IFileService; // Giả sử em có interface này

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements IFileService {

    private final Cloudinary cloudinary;

    @Override
    public List<String> uploadFiles(MultipartFile[] files) {
        List<String> listUrls = new ArrayList<>();

        // Dùng vòng lặp để upload từng file
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue; // Bỏ qua file rỗng nếu có
            }
            try {
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
                String fileUrl = (String) uploadResult.get("secure_url");
                listUrls.add(fileUrl);
            } catch (IOException e) {
                throw new RuntimeException("Không thể upload file: " + file.getOriginalFilename(), e);
            }
        }

        return listUrls;
    }
}
