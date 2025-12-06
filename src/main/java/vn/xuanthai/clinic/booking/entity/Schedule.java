package vn.xuanthai.clinic.booking.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.xuanthai.clinic.booking.enums.ScheduleStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
// Thêm ràng buộc UNIQUE kết hợp trên 3 cột
@Table(name = "schedules",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"doctor_id", "date", "time_slot"})
        },
        indexes = {
                @Index(name = "idx_schedule_doctor_date", columnList = "doctor_id, date")
        }
)
@Getter
@Setter
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "time_slot", length = 50, nullable = false)
    private String timeSlot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ScheduleStatus status = ScheduleStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    @JsonBackReference("doctor-schedules")
    private Doctor doctor;

    @OneToMany(mappedBy = "schedule", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference("schedule-appointment")
    private List<Appointment> appointments = new ArrayList<>();
}
