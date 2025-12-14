package vn.xuanthai.clinic.booking.utils.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.xuanthai.clinic.booking.dto.response.AppointmentCancelledEvent;
import vn.xuanthai.clinic.booking.entity.Appointment;
import vn.xuanthai.clinic.booking.entity.Schedule;
import vn.xuanthai.clinic.booking.enums.AppointmentStatus;
import vn.xuanthai.clinic.booking.enums.ScheduleStatus;
import vn.xuanthai.clinic.booking.repository.AppointmentRepository;
import vn.xuanthai.clinic.booking.repository.ScheduleRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentCleanupTask {

    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Chạy mỗi 1 phút (60000ms) một lần
    // fixedRate: Tính từ lúc task bắt đầu chạy
    @Scheduled(fixedRate = 60000)
    @Transactional // Để đảm bảo update cả Appointment và Schedule cùng lúc
    public void autoCancelUnpaidAppointments() {

        // 1. Tính mốc thời gian: Lấy giờ hiện tại trừ đi 15 phút
        LocalDateTime cutOffTime = LocalDateTime.now().minusMinutes(15);

        // 2. Tìm các đơn "Chờ thanh toán" mà tạo trước mốc thời gian trên (tức là đã quá 15p)
        List<Appointment> expiredAppointments = appointmentRepository.findByStatusAndCreatedAtBefore(
                AppointmentStatus.PENDING_PAYMENT,
                cutOffTime
        );

        if (expiredAppointments.isEmpty()) {
            return; // Không có ai hết hạn thì thôi
        }

        log.info("Tìm thấy {} lịch hẹn quá hạn thanh toán. Đang xử lý hủy...", expiredAppointments.size());

        // 3. Duyệt và hủy từng cái
        for (Appointment appointment : expiredAppointments) {
            // A. Đổi trạng thái lịch hẹn sang CANCELLED
            appointment.setStatus(AppointmentStatus.CANCELLED);

            // B. Quan trọng: NHẢ LỊCH RA (Schedule) để người khác đặt
            Schedule schedule = appointment.getSchedule();
            schedule.setStatus(ScheduleStatus.AVAILABLE);

            // C. Lưu lại
            scheduleRepository.save(schedule); // Lưu schedule
            appointmentRepository.save(appointment); // Lưu appointment

            // GỬI KAFKA THÔNG BÁO
            try {
                AppointmentCancelledEvent event = new AppointmentCancelledEvent(
                        appointment.getPatient().getEmail(),
                        appointment.getPatient().getFullName(),
                        schedule.getDoctor().getUser().getFullName(),
                        schedule.getDate(),
                        schedule.getTimeSlot(),
                        "Hệ thống tự động hủy do quá hạn thanh toán (15 phút)." // Lý do hủy
                );

                // Gửi message
                kafkaTemplate.send("appointment-cancelled-topic", event);

            } catch (Exception e) {
                // Quan trọng: Chỉ log lỗi, KHÔNG throw exception
                // Để đảm bảo việc hủy đơn trong DB vẫn thành công dù gửi mail lỗi
                log.error("Lỗi gửi Kafka trong CronJob cho lịch hẹn ID {}: {}", appointment.getId(), e.getMessage());
            }
        }

        log.info("Đã hủy thành công {} lịch hẹn.", expiredAppointments.size());
    }
}