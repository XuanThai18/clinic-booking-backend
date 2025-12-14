package vn.xuanthai.clinic.booking.utils.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import vn.xuanthai.clinic.booking.dto.response.AppointmentBookedEvent;
import vn.xuanthai.clinic.booking.dto.response.AppointmentCancelledEvent;
import vn.xuanthai.clinic.booking.utils.IEmailService;

@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {

    private final JavaMailSender javaMailSender;

    // Gửi email đơn giản
    @Override
    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("nxt882004@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
    }

    @Override
    public void sendConfirmationToPatient(String email, AppointmentBookedEvent event) {
        try {
            // 1. Tạo message
            MimeMessage message = javaMailSender.createMimeMessage();

            // Helper giúp set thông tin dễ hơn, true nghĩa là hỗ trợ Multipart (gửi file đính kèm nếu cần)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("nxt882004@gmail.com");
            helper.setTo(email);
            helper.setSubject("Xác nhận đặt lịch khám bệnh thành công");

            // 2. Tạo nội dung HTML cho đẹp
            String htmlContent = String.format("""
                <div style="font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd;">
                    <h2 style="color: #2c3e50;">Đặt lịch thành công!</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Cảm ơn bạn đã đặt lịch khám tại phòng khám của chúng tôi. Dưới đây là thông tin chi tiết:</p>
                    <ul>
                        <li><strong>Ngày khám:</strong> %s</li>
                        <li><strong>Giờ khám:</strong> %s</li>
                        <li><strong>Bác sĩ phụ trách:</strong> %s</li>
                    </ul>
                    <p style="color: red;">Vui lòng đến trước 15 phút để làm thủ tục.</p>
                    <br>
                    <p>Trân trọng,<br>Đội ngũ phòng khám.</p>
                </div>
                """,
                    event.getPatientName(),
                    event.getDate(),
                    event.getTimeSlot(),
                    event.getDoctorName()
            );

            // true ở đây để bật chế độ HTML
            helper.setText(htmlContent, true);

            // 3. Gửi mail
            javaMailSender.send(message);

        } catch (MessagingException e) {
            // Log lỗi nhưng KHÔNG ném ngoại lệ (throw) để tránh làm chết Kafka Consumer
            // Nếu lỗi gửi mail, ta chỉ log lại để admin kiểm tra sau
            System.err.println("Lỗi khi gửi email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendCancellationEmail(AppointmentCancelledEvent event) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(event.getRecipientEmail());
        // Tiêu đề email
        helper.setSubject("Thông báo quan trọng về Lịch Hẹn - Phòng khám Xuân Thái");

        // Lấy nội dung thông báo từ Event
        String messageContent = event.getReason();
        

        String htmlContent = String.format("""
        <div style="font-family: Arial, sans-serif; padding: 20px; border: 1px solid #e74c3c; border-radius: 8px;">
            <h2 style="color: #c0392b; text-align: center;">CẬP NHẬT TRẠNG THÁI LỊCH HẸN</h2>
            <p>Xin chào <strong>%s</strong>,</p>
            
            <p>Chúng tôi gửi email này để thông báo về lịch hẹn với Bác sĩ <strong>%s</strong>:</p>
            
            <div style="background-color: #fcebeb; padding: 15px; margin: 20px 0; border-left: 5px solid #c0392b;">
                <strong>TRẠNG THÁI: %s</strong>
            </div>

            <ul>
                <li><strong>Ngày:</strong> %s</li>
                <li><strong>Giờ:</strong> %s</li>
            </ul>

            <p>Nếu có bất kỳ thắc mắc nào về việc hoàn tiền, vui lòng liên hệ hotline: 1900 xxxx.</p>
            <br>
            <p>Trân trọng,<br>Đội ngũ phòng khám Xuân Thái.</p>
        </div>
        """,
                event.getPatientName(),
                event.getDoctorName(),
                messageContent, // <-- Điền nội dung thông báo vào đây (Quan trọng)
                event.getAppointmentDate(),
                event.getTimeSlot()
        );

        helper.setText(htmlContent, true);
        javaMailSender.send(message);
    }
}
