package ma.ensam.okanetransfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ma.ensam.okanetransfer.domain.finance.CashMovement;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CashMovementRepository extends JpaRepository<CashMovement, Long> {
    List<CashMovement> findByCashRegisterId(Long cashRegisterId);
    List<CashMovement> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}