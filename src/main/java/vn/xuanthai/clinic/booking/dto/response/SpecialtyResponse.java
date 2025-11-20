package vn.xuanthai.clinic.booking.dto.response;

import lombok.Data;

import java.util.Set;

@Data
public class SpecialtyResponse {
    private Long id;
    private String name;
    private String description;
    private Set<String> imageUrls;
}