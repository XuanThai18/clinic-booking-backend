package vn.xuanthai.clinic.booking.utils.impl;

import org.springframework.stereotype.Service;
import vn.xuanthai.clinic.booking.utils.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void pushToDoctorDashboard(String doctorName, String message) {
        // TODO: Sau này sẽ tích hợp WebSocket (SimpMessagingTemplate) ở đây
        System.out.println(">>>>> [PUSH NOTIFICATION] Gửi tới Bác sĩ ID: " + doctorName);
        System.out.println(">>>>> Nội dung: " + message);
    }
}