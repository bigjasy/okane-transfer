package ma.ensam.okanetransfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ma.ensam.okanetransfer.domain.transfer.Transfer;
import ma.ensam.okanetransfer.enums.TransferStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {
    Optional<Transfer> findByReference(String reference);
    List<Transfer> findByStatus(TransferStatus status);
    List<Transfer> findBySenderId(Long senderId);
    // Correspond à findByBeneficiaryPhone demandé dans la doc
    List<Transfer> findByBeneficiaryPhoneNumber(String phoneNumber); 
    List<Transfer> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}