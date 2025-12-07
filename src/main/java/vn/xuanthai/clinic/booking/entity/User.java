package vn.xuanthai.clinic.booking.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import vn.xuanthai.clinic.booking.config.StringCryptoConverter;
import vn.xuanthai.clinic.booking.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clinic_id")
    private Long clinicId; // Null nếu là Super Admin hoặc Bệnh nhân tự do

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone_number", length = 255, unique = true)
    @Convert(converter = StringCryptoConverter.class)
    private String phoneNumber;

    @Column(name = "address", columnDefinition = "TEXT")
    @Convert(converter = StringCryptoConverter.class)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "is_active", nullable = false)
    public boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "lockout_end_time")
    private LocalDateTime lockoutEndTime;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @JsonManagedReference("user-roles") // Đặt tên định danh
    private Set<Role> roles = new HashSet<>();

    // --- THÊM MỚI: QUYỀN RIÊNG LẺ (EXTRA PERMISSIONS) ---
    // Đây là các quyền được cấp thêm ngoài quyền của Role
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_permissions", // Bảng phụ mới
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> extraPermissions = new HashSet<>();

    // Token reset mật khẩu
    private String resetPasswordToken;

    // Thời gian hết hạn của token
    private LocalDateTime resetPasswordTokenExpiry;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("user-doctor")
    private Doctor doctor; // Một User có thể có một hồ sơ Doctor

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("user-appointments")
    private Set<Appointment> appointments = new HashSet<>();
}
