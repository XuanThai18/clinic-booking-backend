package vn.xuanthai.clinic.booking.utils.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import vn.xuanthai.clinic.booking.config.VNPayConfig;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {

    public String createPaymentUrl(HttpServletRequest request, long amount, String orderInfo, String vnp_TxnRef) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", VNPayConfig.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");

        if (vnp_TxnRef == null) {
            vnp_TxnRef = VNPayConfig.getRandomNumber(8);
        }
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);

        // --- XỬ LÝ AN TOÀN ORDER INFO ---
        // Thay thế tất cả ký tự không phải chữ số thành gạch dưới (_)
        // Để đảm bảo không có dấu cách hay ký tự lạ gây sai Hash
        String safeOrderInfo = orderInfo.replaceAll("[^a-zA-Z0-9]", "_");
        vnp_Params.put("vnp_OrderInfo", safeOrderInfo);

        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);

        // --- ÉP CỨNG IP ---
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // --- TẠO CHUỖI HASH ---
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                try {
                    // Dùng US_ASCII: Chuẩn an toàn nhất cho Sandbox
                    // Hash Data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    // Query URL
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        // Tạo Hash
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return VNPayConfig.vnp_PayUrl + "?" + queryUrl;
    }
}
