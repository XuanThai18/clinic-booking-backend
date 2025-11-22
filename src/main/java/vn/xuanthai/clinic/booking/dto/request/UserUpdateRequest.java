package vn.xuanthai.clinic.booking.dto.request;

import lombok.Data;
import vn.xuanthai.clinic.booking.enums.Gender;
import java.time.LocalDate;

@Data
public class UserUpdateRequest {
    private String fullName;
    private String phoneNumber;
    private String address;
    private Gender gender;
    private LocalDate birthday;
}