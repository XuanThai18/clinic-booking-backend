package vn.xuanthai.clinic.booking.config;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;

@Converter
@Component // Biến nó thành một Spring component để có thể inject @Value
public class StringCryptoConverter implements AttributeConverter<String, String> {

    // Sử dụng thuật toán an toàn: AES/GCM
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTE = 12; // GCM khuyến nghị dùng IV 12 bytes
    private static final int TAG_LENGTH_BIT = 128; // Kích thước authentication tag

    // Dùng static để "bắc cầu" từ Spring sang JPA
    private static Key staticKey;

    @Value("${application.security.db.encryption-key}")
    private String secretKey; // Trường này sẽ được Spring inject

    // Phương thức này sẽ được Spring gọi sau khi đã inject các giá trị
    @PostConstruct
    public void init() {
        // Đảm bảo key có độ dài hợp lệ (16, 24, hoặc 32 bytes)
        if (secretKey.length() != 16 && secretKey.length() != 24 && secretKey.length() != 32) {
            throw new IllegalArgumentException("Khóa mã hóa phải có độ dài 16, 24, hoặc 32 bytes.");
        }
        // Gán key đã được inject vào biến static
        staticKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
    }

    // Mã hóa: Plain Text -> Ciphertext (đã bao gồm IV)
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            // 1. Tạo một IV ngẫu nhiên, an toàn
            byte[] iv = new byte[IV_LENGTH_BYTE];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, staticKey, parameterSpec);

            // 2. Mã hóa dữ liệu
            byte[] cipherText = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            // 3. Nối IV vào đầu ciphertext để lưu trữ
            byte[] ivAndCipherText = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, ivAndCipherText, 0, iv.length);
            System.arraycopy(cipherText, 0, ivAndCipherText, iv.length, cipherText.length);

            // 4. Encode Base64 để lưu vào DB dưới dạng String
            return Base64.getEncoder().encodeToString(ivAndCipherText);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi mã hóa dữ liệu", e);
        }
    }

    // Giải mã: Ciphertext (đã bao gồm IV) -> Plain Text
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        try {
            // 1. Decode Base64
            byte[] ivAndCipherText = Base64.getDecoder().decode(dbData);

            // 2. Tách IV và ciphertext ra
            byte[] iv = new byte[IV_LENGTH_BYTE];
            System.arraycopy(ivAndCipherText, 0, iv, 0, iv.length);

            byte[] cipherText = new byte[ivAndCipherText.length - iv.length];
            System.arraycopy(ivAndCipherText, iv.length, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, staticKey, parameterSpec);

            // 3. Giải mã
            byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi giải mã dữ liệu", e);
        }
    }
}