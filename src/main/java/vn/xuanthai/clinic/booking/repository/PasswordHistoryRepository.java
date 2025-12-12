package vn.xuanthai.clinic.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.xuanthai.clinic.booking.entity.PasswordHistory;
import vn.xuanthai.clinic.booking.entity.User;
import java.util.List;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    //  dùng Pageable cho rõ ràng
    List<PasswordHistory> findByUser(User user, Pageable pageable);

    // Lấy danh sách ID của user, sắp xếp theo thời gian tăng dần
    @Query("SELECT ph.id FROM PasswordHistory ph WHERE ph.user = :user ORDER BY ph.createdAt ASC")
    List<Long> findIdsByUserOrderByCreatedAtAsc(User user);

    // Hỗ trợ xóa theo danh sách ID (tối ưu hơn deleteAllById từng cái)
    void deleteAllByIdInBatch(Iterable<Long> ids);
}