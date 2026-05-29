package ma.ensam.okanetransfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ma.ensam.okanetransfer.domain.finance.Commission;
import java.util.List;

@Repository
public interface CommissionRepository extends JpaRepository<Commission, Long> {
    List<Commission> findByTransferId(Long transferId);
    List<Commission> findByAgencyId(Long agencyId);
}