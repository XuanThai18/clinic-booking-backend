package vn.xuanthai.clinic.booking.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import vn.xuanthai.clinic.booking.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String address;
    private Gender gender;
    private LocalDate birthday;
    @JsonProperty("isActive")
    private boolean isActive;
    private LocalDateTime createdAt;
    private Set<String> roles; // Chỉ trả về tên của các role
}