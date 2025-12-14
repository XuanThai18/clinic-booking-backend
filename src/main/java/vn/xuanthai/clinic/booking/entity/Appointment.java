package vn.xuanthai.clinic.booking.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import vn.xuanthai.clinic.booking.enums.AppointmentStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_appointment_patientid", columnList = "patient_id"),
        @Index(name = "idx_appointment_scheduleid", columnList = "schedule_id")
})
@Getter
@Setter
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "schedule_id", nullable = false)
    @JsonBackReference("schedule-appointment")
    private Schedule schedule;

    // Mối quan hệ Nhiều-1 với User (bệnh nhân)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonBackReference("user-appointments")
    private User patient;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis; // Chẩn đoán

    @Column(name = "prescription", columnDefinition = "TEXT")
    private String prescription; // Đơn thuốc / Ghi chú

    @Enumerated(EnumType.STRING) // Quan trọng: Báo cho JPA lưu tên của Enum (ví dụ: "CONFIRMED")
    @Column(name = "status", length = 20, nullable = false)
    private AppointmentStatus status;

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
