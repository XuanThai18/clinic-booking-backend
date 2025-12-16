package vn.xuanthai.clinic.booking.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@Configuration
public class VNPayConfig {
    public static String vnp_PayUrl;
    public static String vnp_ReturnUrl;
    public static String vnp_TmnCode;
    public static String secretKey;
    public static String vnp_ApiUrl;

    @Value("${vnpay.pay-url}")
    public void setVnp_PayUrl(String payUrl) {
        VNPayConfig.vnp_PayUrl = payUrl;
    }

    @Value("${vnpay.return-url}")
    public void setVnp_ReturnUrl(String returnUrl) {
        VNPayConfig.vnp_ReturnUrl = returnUrl;
    }

    @Value("${vnpay.tmn-code}")
    public void setVnp_TmnCode(String tmnCode) {
        VNPayConfig.vnp_TmnCode = tmnCode;
    }

    @Value("${vnpay.secret-key}")
    public void setSecretKey(String secretKey) {
        VNPayConfig.secretKey = secretKey;
    }

    @Value("${vnpay.api-url}")
    public void setVnp_ApiUrl(String apiUrl) {
        VNPayConfig.vnp_ApiUrl = apiUrl;
    }

    // Thuật toán mã hóa HMACSHA512
    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");

            // --- QUAN TRỌNG: Ép kiểu byte của Key thành UTF-8 ---
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);

            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);

            // Dữ liệu hash cũng ép về UTF-8
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);

            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception ex) {
            return "";
        }
    }

    // Hàm lấy IP
    public static String getIpAddress(HttpServletRequest request) {
        String ipAdress;
        try {
            ipAdress = request.getHeader("X-FORWARDED-FOR");
            if (ipAdress == null) {
                ipAdress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAdress = "Invalid IP:" + e.getMessage();
        }
        //return ipAdress;
        return "127.0.0.1";
    }

    // Hàm random số
    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}