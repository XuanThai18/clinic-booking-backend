package vn.xuanthai.clinic.booking.dto.response;

import lombok.Data;

import java.util.Set;

@Data
public class ClinicResponse {
    private Long id;
    private String name;
    private String address;
    private String phoneNumber;
    private String description;
    private Set<String> imageUrls;
}