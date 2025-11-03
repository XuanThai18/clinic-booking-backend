package vn.xuanthai.clinic.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.xuanthai.clinic.booking.entity.Specialty;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

}
