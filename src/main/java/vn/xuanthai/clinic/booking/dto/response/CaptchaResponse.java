package vn.xuanthai.clinic.booking.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CaptchaResponse {
    private boolean success;
    @JsonProperty("error-codes") // Map tên JSON với tên biến Java
    private String[] errorCodes;
}