package ma.ensam.okanetransfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import ma.ensam.okanetransfer.domain.finance.Commission;
import java.util.List;

@Repository
public interface CommissionRepository extends JpaRepository<Commission, Long> {
    List<Commission> findByTransferId(Long transferId);
    List<Commission> findByAgencyId(Long agencyId);

    Page<Commission> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Commission> findByAgencyIdOrderByCreatedAtDesc(Long agencyId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(c.agencyPart), 0) FROM Commission c WHERE (:agencyId IS NULL OR c.agency.id = :agencyId)")
    BigDecimal sumAgencyCommission(@Param("agencyId") Long agencyId);

    @Query("SELECT COALESCE(SUM(c.centralPart), 0) FROM Commission c WHERE (:agencyId IS NULL OR c.agency.id = :agencyId)")
    BigDecimal sumCentralCommission(@Param("agencyId") Long agencyId);
}