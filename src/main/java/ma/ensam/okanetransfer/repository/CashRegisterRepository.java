package ma.ensam.okanetransfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ma.ensam.okanetransfer.domain.finance.CashRegister;
import ma.ensam.okanetransfer.enums.CashRegisterStatus;
import java.util.List;
import java.util.Optional;

@Repository
public interface CashRegisterRepository extends JpaRepository<CashRegister, Long> {
    Optional<CashRegister> findByAgentIdAndStatus(Long agentId, CashRegisterStatus status);
    List<CashRegister> findByAgencyIdAndStatus(Long agencyId, CashRegisterStatus status);
}