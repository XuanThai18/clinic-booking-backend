package vn.xuanthai.clinic.booking.service;

import vn.xuanthai.clinic.booking.dto.request.ScheduleCreateRequest;
import vn.xuanthai.clinic.booking.dto.response.ScheduleResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.core.Authentication;

public interface IScheduleService {

    /**
     * Bác sĩ tạo lịch làm việc cho một ngày (gồm nhiều khung giờ).
     * @return Danh sách các khung giờ đã được tạo thành công.
     */
    List<ScheduleResponse> createDoctorSchedule(ScheduleCreateRequest request, Authentication authentication);

    /**
     * Bệnh nhân (hoặc Admin) xem các khung giờ làm việc của một bác sĩ theo ngày.
     */
    List<ScheduleResponse> getSchedulesByDoctorAndDate(Long doctorId, LocalDate date);

    /**
     * (Nâng cao) Bệnh nhân xem các khung giờ CÒN TRỐNG của bác sĩ.
     */
    List<ScheduleResponse> getAvailableSchedulesByDoctorAndDate(Long doctorId, LocalDate date);
}