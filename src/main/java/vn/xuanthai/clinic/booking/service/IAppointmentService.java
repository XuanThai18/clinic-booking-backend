package vn.xuanthai.clinic.booking.service;

import org.springframework.security.core.Authentication;
import vn.xuanthai.clinic.booking.dto.request.AppointmentRequest;
import vn.xuanthai.clinic.booking.dto.response.AppointmentResponse;
import vn.xuanthai.clinic.booking.enums.AppointmentStatus;

import java.util.List;

public interface IAppointmentService {

    /**
     * Bệnh nhân tạo một lịch hẹn mới.
     * Đây là phương thức phức tạp nhất.
     */
    AppointmentResponse createAppointment(AppointmentRequest request, Authentication authentication);

    /**
     * Bệnh nhân xem lịch sử đặt lịch của chính mình.
     */
    List<AppointmentResponse> getMyAppointments(Authentication authentication);

    /**
     * Admin xem tất cả các lịch hẹn.
     */
    List<AppointmentResponse> getAllAppointments();

    void updateStatus(Long appointmentId, AppointmentStatus status);

    /**
     * Bệnh nhân (hoặc Admin) hủy một lịch hẹn.
     */
    AppointmentResponse cancelAppointment(Long appointmentId, Authentication authentication);
}