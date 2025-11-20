package vn.xuanthai.clinic.booking.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "specialties")
@Getter
@Setter
public class Specialty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER) // Load ảnh luôn khi lấy chuyên khoa
    @CollectionTable(
            name = "specialty_images", // Tên bảng phụ sẽ được tự động tạo
            joinColumns = @JoinColumn(name = "specialty_id")
    )
    @Column(name = "image_url") // Tên cột trong bảng phụ
    private Set<String> imageUrls = new HashSet<>();

    @OneToMany(mappedBy = "specialty", fetch = FetchType.LAZY)
    @JsonManagedReference("specialty-doctors")
    private Set<Doctor> doctors = new HashSet<>();
}
