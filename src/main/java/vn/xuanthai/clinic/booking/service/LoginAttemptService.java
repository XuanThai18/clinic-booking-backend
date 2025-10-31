package vn.xuanthai.clinic.booking.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    public static final int MAX_ATTEMPT = 5; // Số lần sai tối đa
    private LoadingCache<String, Integer> attemptsCache;

    public LoginAttemptService() {
        super();
        // Tạo cache: lưu tối đa 1000 key, hết hạn sau 15 phút kể từ lần ghi cuối
        attemptsCache = CacheBuilder.newBuilder().
                expireAfterWrite(15, TimeUnit.MINUTES).maximumSize(1000).build(new CacheLoader<String, Integer>() {
                    public Integer load(String key) {
                        return 0; // Giá trị mặc định là 0
                    }
                });
    }

    // Gọi khi đăng nhập thành công
    public void loginSucceeded(String key) {
        attemptsCache.invalidate(key); // Xóa key khỏi cache
    }

    // Gọi khi đăng nhập thất bại
    public void loginFailed(String key) {
        int attempts;
        try {
            attempts = attemptsCache.get(key);
        } catch (ExecutionException e) {
            attempts = 0;
        }
        attempts++;
        attemptsCache.put(key, attempts);
    }

    // Kiểm tra xem user có bị khóa không
    public boolean isBlocked(String key) {
        try {
            return attemptsCache.get(key) >= MAX_ATTEMPT;
        } catch (ExecutionException e) {
            return false;
        }
    }
}