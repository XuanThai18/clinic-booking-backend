package vn.xuanthai.clinic.booking.service;

import org.springframework.security.core.Authentication;
import vn.xuanthai.clinic.booking.dto.request.AppointmentRequest;
import vn.xuanthai.clinic.booking.dto.request.BookingRequest;
import vn.xuanthai.clinic.booking.dto.request.CompletionRequest;
import vn.xuanthai.clinic.booking.dto.response.AppointmentResponse;
import vn.xuanthai.clinic.booking.enums.AppointmentStatus;

import java.util.List;

public interface IAppointmentService {

    /**
     * Bệnh nhân tạo một lịch hẹn mới.
     * Đây là phương thức phức tạp nhất.
     */
    AppointmentResponse createAppointment(AppointmentRequest request, Authentication authentication);

    List<AppointmentResponse> getMyAppointments(); // Cho bệnh nhân
    List<AppointmentResponse> getAllAppointmentsForDoctor(); // Cho bác sĩ (Nhớ thêm dòng này)
    List<AppointmentResponse> getAllAppointments(); // Cho Admin

    void updateStatus(Long appointmentId, AppointmentStatus status);

    // Hàm mới: Hoàn thành cuộc hẹn và lưu kết quả
    void completeAppointment(Long appointmentId, CompletionRequest request);

    // admin hủy lịch hẹn tất cả trạng thái
    void deleteAppointment(Long id);
    /**
     * Bệnh nhân hủy một lịch hẹn.
     */
    AppointmentResponse cancelAppointment(Long appointmentId, Authentication authentication);

    AppointmentResponse bookAppointment(BookingRequest request);

    AppointmentResponse processRefund(Long appointmentId);
}