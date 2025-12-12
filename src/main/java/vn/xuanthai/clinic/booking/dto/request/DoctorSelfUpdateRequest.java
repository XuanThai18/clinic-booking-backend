package vn.xuanthai.clinic.booking.dto.request;

import lombok.Data;

import java.util.Set;

@Data
public class DoctorSelfUpdateRequest {
    private String description;
    private String academicDegree;
    private String image;
    private Set<String> otherImages;
}