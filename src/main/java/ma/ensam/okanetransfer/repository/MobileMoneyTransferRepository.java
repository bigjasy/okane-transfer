package ma.ensam.okanetransfer.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import ma.ensam.okanetransfer.domain.transfer.MobileMoneyTransfer;
import ma.ensam.okanetransfer.enums.MobileMoneyOperator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MobileMoneyTransferRepository extends JpaRepository<MobileMoneyTransfer, Long> {

    Optional<MobileMoneyTransfer> findByTransferId(Long transferId);

    @Query("SELECT m FROM MobileMoneyTransfer m WHERE m.transfer.reference = :reference")
    Optional<MobileMoneyTransfer> findByTransferReference(@Param("reference") String reference);

    Page<MobileMoneyTransfer> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
            SELECT m FROM MobileMoneyTransfer m
            WHERE m.transfer.createdByAgent.id = :agentId
            ORDER BY m.createdAt DESC
            """)
    Page<MobileMoneyTransfer> findByAgentId(@Param("agentId") Long agentId, Pageable pageable);

    @Query("""
            SELECT m FROM MobileMoneyTransfer m
            WHERE m.transfer.sourceAgency.id = :agencyId
               OR m.transfer.destinationAgency.id = :agencyId
            ORDER BY m.createdAt DESC
            """)
    Page<MobileMoneyTransfer> findByAgencyId(@Param("agencyId") Long agencyId, Pageable pageable);

    List<MobileMoneyTransfer> findByOperatorAndCreatedAtBetween(
            MobileMoneyOperator operator,
            LocalDateTime start,
            LocalDateTime end
    );
}
