package vn.xuanthai.clinic.booking.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "doctors")
@Getter
@Setter
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "academic_degree", length = 100)
    private String academicDegree;

    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;

    // Mối quan hệ 1-1 với User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    @JsonBackReference("user-doctor")
    private User user;

    // Mối quan hệ Nhiều-1 với Specialty
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id")
    @JsonBackReference("specialty-doctors")
    private Specialty specialty;

    //Mối quan hệ Nhiều-1 với clinics
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id")
    @JsonBackReference("clinic-doctors")
    private Clinic clinic;

    // Hoàn thiện mối quan hệ 2 chiều với Schedule
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("doctor-schedules")
    private Set<Schedule> schedules = new HashSet<>();
}
