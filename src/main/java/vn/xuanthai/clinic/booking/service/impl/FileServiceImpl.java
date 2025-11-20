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

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) return;

        try {
            // 1. Lấy public_id từ URL
            String publicId = getPublicIdFromUrl(fileUrl);

            // 2. Gọi lệnh xóa trên Cloudinary
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

        } catch (IOException e) {
            // Chỉ log lỗi, không nên throw exception để tránh làm gián đoạn quy trình chính
            System.err.println("Không thể xóa file trên Cloudinary: " + fileUrl);
            e.printStackTrace();
        }
    }

    // Hàm trợ giúp: Trích xuất public_id từ URL
    // Ví dụ: https://res.cloudinary.com/.../upload/v123456/folder/my-image.jpg
    // -> public_id: folder/my-image
    private String getPublicIdFromUrl(String url) {
        try {
            // Cắt bỏ phần extension (.jpg, .png)
            int dotIndex = url.lastIndexOf(".");
            String urlWithoutExtension = url.substring(0, dotIndex);

            // Lấy phần sau dấu '/' cuối cùng (nếu không dùng folder)
            // Hoặc xử lý phức tạp hơn nếu em dùng folder trên Cloudinary
            // Cách đơn giản nhất cho cấu hình mặc định: lấy phần sau phiên bản /v[số]/

            // Regex để tìm vị trí sau /upload/v[số]/ hoặc /upload/
            // Tuy nhiên, cách đơn giản nhất thường là lấy tên file cuối cùng
            int lastSlashIndex = urlWithoutExtension.lastIndexOf("/");
            return urlWithoutExtension.substring(lastSlashIndex + 1);
        } catch (Exception e) {
            return null;
        }
    }
}
