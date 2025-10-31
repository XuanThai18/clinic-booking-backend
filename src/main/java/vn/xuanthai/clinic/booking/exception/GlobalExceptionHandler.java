package vn.xuanthai.clinic.booking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bắt lỗi validation (@Valid).
     * Khi client gửi dữ liệu không hợp lệ (ví dụ: title trống), lỗi này sẽ được kích hoạt.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // 400 Bad Request
    }

    /**
     * Bắt lỗi "Không tìm thấy tài nguyên" mà chúng ta sẽ tự định nghĩa.
     * Ví dụ: Khi tìm một bác sĩ với ID không tồn tại.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND); // 404 Not Found
    }

    /**
     * Bắt các lỗi yêu cầu không hợp lệ do logic nghiệp vụ.
     * Ví dụ: Cố gắng đặt một lịch đã được đặt.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequestException(BadRequestException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST); // 400 Bad Request
    }

    /**
     * Bắt tất cả các loại lỗi còn lại không được xử lý cụ thể.
     * Đây là chốt chặn cuối cùng để đảm bảo không có lỗi nào bị lọt ra ngoài với định dạng xấu xí.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex) {
        // Ghi log lỗi ở đây để debug
        // logger.error("An unexpected error occurred", ex);
        return new ResponseEntity<>("Đã có lỗi không mong muốn xảy ra trên server.", HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
    }
}