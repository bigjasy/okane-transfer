package ma.ensam.okanetransfer.repository;

import java.util.List;
import ma.ensam.okanetransfer.domain.compliance.AmlAlert;
import ma.ensam.okanetransfer.enums.AmlAlertStatus;
import ma.ensam.okanetransfer.enums.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmlAlertRepository extends JpaRepository<AmlAlert, Long> {
    Page<AmlAlert> findByStatus(AmlAlertStatus status, Pageable pageable);

    Page<AmlAlert> findByRiskLevel(RiskLevel riskLevel, Pageable pageable);

    List<AmlAlert> findByTransferId(Long transferId);

    long countByStatus(AmlAlertStatus status);

    long countByRiskLevelAndStatus(RiskLevel riskLevel, AmlAlertStatus status);
}
