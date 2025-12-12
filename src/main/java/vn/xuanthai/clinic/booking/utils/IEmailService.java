package vn.xuanthai.clinic.booking.utils;

import vn.xuanthai.clinic.booking.dto.response.AppointmentBookedEvent;

public interface IEmailService {
    void sendEmail(String to, String subject, String text);
    void sendConfirmationToPatient(String email, AppointmentBookedEvent event);
}
