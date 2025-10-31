package vn.xuanthai.clinic.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import vn.xuanthai.clinic.booking.dto.response.CaptchaResponse;
import vn.xuanthai.clinic.booking.exception.BadRequestException;

@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final RestTemplate restTemplate; // Cần tạo Bean RestTemplate trong Config

    @Value("${recaptcha.secret}")
    private String recaptchaSecret;

    @Value("${recaptcha.verify-url}")
    private String recaptchaVerifyUrl;

    public void validateCaptcha(String captchaResponseToken) {
        if (captchaResponseToken == null || captchaResponseToken.isEmpty()) {
            throw new BadRequestException("Mã CAPTCHA không hợp lệ.");
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("secret", recaptchaSecret);
        body.add("response", captchaResponseToken);

        CaptchaResponse response = restTemplate.postForObject(recaptchaVerifyUrl, body, CaptchaResponse.class);

        if (response == null || !response.isSuccess()) {
            throw new BadRequestException("Xác minh CAPTCHA thất bại.");
        }
    }
}
