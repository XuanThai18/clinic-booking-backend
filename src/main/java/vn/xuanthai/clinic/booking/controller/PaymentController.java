package vn.xuanthai.clinic.booking.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.xuanthai.clinic.booking.dto.response.AppointmentBookedEvent;
import vn.xuanthai.clinic.booking.entity.Appointment;
import vn.xuanthai.clinic.booking.entity.User;
import vn.xuanthai.clinic.booking.enums.AppointmentStatus;
import vn.xuanthai.clinic.booking.exception.ResourceNotFoundException;
import vn.xuanthai.clinic.booking.repository.AppointmentRepository;
import vn.xuanthai.clinic.booking.service.impl.UserContextService;
import vn.xuanthai.clinic.booking.utils.impl.VNPayService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final VNPayService vnPayService;
    private final UserContextService userContextService;
    private final AppointmentRepository appointmentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate; // Inject Kafka

    @GetMapping("/create-payment")
    public ResponseEntity<?> createPayment(HttpServletRequest request,
                                           @RequestParam Long appointmentId) {

        // 1. Lấy User đang đăng nhập (BẮT BUỘC ĐỂ CHECK BẢO MẬT)
        User currentUser = userContextService.getCurrentUser();

        // 2. Tìm thông tin lịch hẹn
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn với ID: " + appointmentId));

        // --- CHECK BẢO MẬT (QUAN TRỌNG NHẤT) ---
        // Ngăn chặn việc User A thanh toán hộ hoặc xem trộm đơn của User B
        if (!appointment.getPatient().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body("Bạn không có quyền thanh toán cho lịch hẹn này!");
        }

        // --- CHECK NGHIỆP VỤ ---
        // Nếu đơn đã thanh toán rồi hoặc đã hủy thì không cho tạo link nữa
        if (appointment.getStatus() == AppointmentStatus.CONFIRMED ||
                appointment.getStatus() == AppointmentStatus.COMPLETED) {
            return ResponseEntity.badRequest().body("Lịch hẹn này đã được thanh toán rồi!");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return ResponseEntity.badRequest().body("Lịch hẹn này đã bị hủy!");
        }

        // 3. Lấy giá khám (Logic cũ của bạn - OK)
        BigDecimal doctorPrice = appointment.getSchedule().getDoctor().getPrice();
        BigDecimal finalPrice = (doctorPrice != null) ? doctorPrice : BigDecimal.valueOf(200000);
        long amount = finalPrice.longValue();

        // 4. Tạo nội dung thanh toán (Logic cũ của bạn - OK)
        String orderInfo = "ThanhToanLichHen_" + appointmentId;

        // 5. TẠO MÃ GIAO DỊCH
        String vnp_TxnRef = ("APT" + appointmentId + "_" + System.currentTimeMillis());

        // 6. Gọi VNPay Service
        String paymentUrl = vnPayService.createPaymentUrl(request, amount, orderInfo, vnp_TxnRef);

        // Trả về JSON
        // Ví dụ: { "url": "https://..." }
        return ResponseEntity.ok(Map.of("url", paymentUrl));
    }

    // 2. API nhận kết quả từ VNPay
    @GetMapping("/vnpay-return")
    @Transactional // Quan trọng: Để đảm bảo update DB và gửi Kafka cùng pha
    public ResponseEntity<?> paymentReturn(HttpServletRequest request) {
        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
        String vnp_OrderInfo = request.getParameter("vnp_OrderInfo");
        String vnp_TxnRef = request.getParameter("vnp_TxnRef"); // Mã giao dịch (thường là ID đơn hàng)

        if ("00".equals(vnp_ResponseCode)) {
            // 1. Lấy ID lịch hẹn
            // Cách tốt nhất là dùng vnp_TxnRef nếu bạn gửi nó là appointmentId lúc tạo URL
            // Nếu dùng OrderInfo như cũ thì parse:
            Long appointmentId = extractIdFromInfo(vnp_OrderInfo);

            Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);

            // Kiểm tra: Có tồn tại và trạng thái chưa phải là CONFIRMED (tránh xử lý trùng lặp)
            if (appointment != null && appointment.getStatus() != AppointmentStatus.CONFIRMED) {

                // . Cập nhật trạng thái
                appointment.setStatus(AppointmentStatus.CONFIRMED);
                // . Update thời gian thanh toán thực tế
                appointment.setPaymentTime(LocalDateTime.now());
                appointmentRepository.save(appointment);

                // 3. GỬI KAFKA
                try {
                    // Lấy thông tin từ các bảng quan hệ (Join)
                    // Lưu ý: Đảm bảo JPA fetch được (EAGER hoặc đang trong Transaction)
                    String patientEmail = appointment.getPatient().getEmail();
                    String patientName = appointment.getPatient().getFullName();
                    String doctorName = appointment.getSchedule().getDoctor().getUser().getFullName();

                    AppointmentBookedEvent event = new AppointmentBookedEvent(
                            appointment.getId(),
                            patientEmail,
                            patientName,
                            doctorName,
                            appointment.getSchedule().getDate(),
                            appointment.getSchedule().getTimeSlot()
                    );

                    kafkaTemplate.send("appointment-booked-topic", event);
                    log.info("Check Kafka: Đã gửi event xác nhận thanh toán cho lịch hẹn #{}", appointmentId);

                } catch (Exception e) {
                    log.error("Lỗi gửi Kafka: ", e);
                    // Không throw exception để tránh rollback việc set CONFIRMED
                    // Có thể lưu log vào bảng 'FailedNotification' để retry sau
                }

                // Redirect về trang Success
                return ResponseEntity.status(302)
                        .header("Location", "http://localhost:5173/payment-success?id=" + appointmentId)
                        .build();
            }
        }

        // Thất bại
        return ResponseEntity.status(302)
                .header("Location", "http://localhost:3000/payment-failed")
                .build();
    }

    private Long extractIdFromInfo(String info) {
        try {
            return Long.parseLong(info.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}