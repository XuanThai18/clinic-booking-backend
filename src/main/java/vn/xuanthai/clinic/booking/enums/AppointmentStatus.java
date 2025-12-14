package vn.xuanthai.clinic.booking.enums;

public enum AppointmentStatus {
    PENDING_PAYMENT, // Chờ thanh toán
    CONFIRMED,       // Đã thanh toán (Thành công)
    REFUND_PENDING,  // Đã hủy - Chờ hoàn tiền
    CANCELLED,       // Đã hủy hoàn toàn (Tiền nong đã xong)
    COMPLETED        // Đã khám xong
}
