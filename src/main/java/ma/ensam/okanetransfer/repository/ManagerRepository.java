package ma.ensam.okanetransfer.repository;

import java.util.List;
import ma.ensam.okanetransfer.domain.user.Manager;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagerRepository extends JpaRepository<Manager, Long> {
    List<Manager> findByAgencyId(Long agencyId);
}
