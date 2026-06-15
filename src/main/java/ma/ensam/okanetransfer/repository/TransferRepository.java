package ma.ensam.okanetransfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ma.ensam.okanetransfer.domain.transfer.Transfer;
import ma.ensam.okanetransfer.enums.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {
    Optional<Transfer> findByReference(String reference);
    List<Transfer> findByStatus(TransferStatus status);
    List<Transfer> findBySenderId(Long senderId);
    Page<Transfer> findBySenderId(Long senderId, Pageable pageable);
    Page<Transfer> findByCreatedByAgentId(Long agentId, Pageable pageable);
    List<Transfer> findByBeneficiaryPhoneNumber(String phoneNumber);
    List<Transfer> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("""
            SELECT t FROM Transfer t
            WHERE t.sourceAgency.id = :agencyId OR t.destinationAgency.id = :agencyId
            """)
    Page<Transfer> findByAgencyId(@Param("agencyId") Long agencyId, Pageable pageable);

    @Query("""
            SELECT t FROM Transfer t
            WHERE t.createdByAgent.id = :agentId
               OR t.sourceAgency.id = :agencyId
               OR t.destinationAgency.id = :agencyId
            """)
    Page<Transfer> findVisibleForAgent(
            @Param("agentId") Long agentId,
            @Param("agencyId") Long agencyId,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(t.sentAmount), 0) FROM Transfer t")
    BigDecimal sumTotalVolume();

    @Query("SELECT COALESCE(SUM(t.feeAmount), 0) FROM Transfer t")
    BigDecimal sumTotalFees();

    @Query("SELECT COALESCE(SUM(t.sentAmount), 0) FROM Transfer t WHERE t.sourceAgency.id = :agencyId")
    BigDecimal sumTotalVolumeByAgency(Long agencyId);

    @Query("SELECT COALESCE(SUM(t.feeAmount), 0) FROM Transfer t WHERE t.sourceAgency.id = :agencyId")
    BigDecimal sumTotalFeesByAgency(Long agencyId);

    long countBySourceAgencyId(Long agencyId);

    @Query("SELECT COALESCE(SUM(t.sentAmount), 0) FROM Transfer t WHERE t.createdByAgent.id = :agentId")
    BigDecimal sumTotalVolumeByAgent(Long agentId);

    @Query("SELECT COALESCE(SUM(t.feeAmount), 0) FROM Transfer t WHERE t.createdByAgent.id = :agentId")
    BigDecimal sumTotalFeesByAgent(Long agentId);

    long countByCreatedByAgentId(Long agentId);

    @Query("SELECT COALESCE(SUM(t.sentAmount), 0) FROM Transfer t WHERE t.sender.id = :clientId")
    BigDecimal sumTotalVolumeByClient(Long clientId);

    long countBySenderId(Long clientId);

    long countByStatus(TransferStatus status);

    long countBySourceAgencyIdAndStatus(Long agencyId, TransferStatus status);

    @Query("""
            SELECT YEAR(t.createdAt), MONTH(t.createdAt), COALESCE(SUM(t.sentAmount), 0)
            FROM Transfer t
            WHERE t.createdAt >= :since
            GROUP BY YEAR(t.createdAt), MONTH(t.createdAt)
            ORDER BY YEAR(t.createdAt), MONTH(t.createdAt)
            """)
    List<Object[]> sumVolumeByMonthSince(@Param("since") LocalDateTime since);

    @Query("""
            SELECT YEAR(t.createdAt), MONTH(t.createdAt), COALESCE(SUM(t.sentAmount), 0)
            FROM Transfer t
            WHERE t.createdAt >= :since AND t.sourceAgency.id = :agencyId
            GROUP BY YEAR(t.createdAt), MONTH(t.createdAt)
            ORDER BY YEAR(t.createdAt), MONTH(t.createdAt)
            """)
    List<Object[]> sumVolumeByMonthSinceByAgency(@Param("since") LocalDateTime since, @Param("agencyId") Long agencyId);

    @Query("""
            SELECT COALESCE(SUM(t.sentAmount), 0) FROM Transfer t
            WHERE t.sourceAgency.id = :agencyId
              AND t.createdAt >= :from AND t.createdAt <= :to
              AND t.status <> ma.ensam.okanetransfer.enums.TransferStatus.CANCELLED
            """)
    BigDecimal sumSentAmountBySourceAgencyBetween(
            @Param("agencyId") Long agencyId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("""
            SELECT COALESCE(SUM(t.sentAmount), 0) FROM Transfer t
            WHERE t.sourceCountry.id = :sourceCountryId
              AND t.destinationCountry.id = :destinationCountryId
              AND t.createdAt >= :from AND t.createdAt <= :to
              AND t.status <> ma.ensam.okanetransfer.enums.TransferStatus.CANCELLED
            """)
    BigDecimal sumSentAmountByCorridorCountriesBetween(
            @Param("sourceCountryId") Long sourceCountryId,
            @Param("destinationCountryId") Long destinationCountryId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
