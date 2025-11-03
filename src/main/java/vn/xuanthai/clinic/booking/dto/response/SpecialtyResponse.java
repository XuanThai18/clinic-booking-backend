package vn.xuanthai.clinic.booking.dto.response;

import lombok.Data;

@Data
public class SpecialtyResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
}