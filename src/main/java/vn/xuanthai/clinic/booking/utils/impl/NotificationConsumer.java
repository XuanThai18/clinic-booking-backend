package vn.xuanthai.clinic.booking.utils.impl;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import vn.xuanthai.clinic.booking.dto.response.AppointmentBookedEvent;
import vn.xuanthai.clinic.booking.dto.response.AppointmentCancelledEvent;
import vn.xuanthai.clinic.booking.utils.NotificationService;

// NotificationConsumer.java
@Component // Hoặc @Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailService emailService;
    private final NotificationService notificationService;

    @KafkaListener(topics = "appointment-booked-topic", groupId = "notification-group")
    public void handleAppointmentBooked(AppointmentBookedEvent event) {
        // 1. Gửi Email cho bệnh nhân (Nhiệm vụ bắt buộc)
        // Code này chạy ngầm, dù gửi mail chậm 2-3s cũng không làm bệnh nhân phải chờ loading trang web
        System.out.println("Đang gửi email xác nhận cho: " + event.getPatientEmail());
        emailService.sendConfirmationToPatient(event.getPatientEmail(), event);

        // 2. Thông báo cho bác sĩ (Real-time)
        // Chỉ đẩy thông báo lên màn hình dashboard của bác sĩ
        System.out.println("Đang push notification cho bác sĩ ID: " + event.getDoctorName());
        notificationService.pushToDoctorDashboard(
                event.getDoctorName(),
                "Bạn có lịch hẹn mới lúc " + event.getTimeSlot() + " ngày " + event.getDate()
        );
    }

    @KafkaListener(topics = "notification-topic", groupId = "notification-group")
    public void listenCancelEvent(AppointmentCancelledEvent event) {
        try {
            emailService.sendCancellationEmail(event);
        } catch (MessagingException e) {

        }
    }
}