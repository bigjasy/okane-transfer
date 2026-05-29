package ma.ensam.okanetransfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ma.ensam.okanetransfer.domain.transfer.TransferPayment;
import java.util.Optional;

@Repository
public interface TransferPaymentRepository extends JpaRepository<TransferPayment, Long> {
    Optional<TransferPayment> findByTransferId(Long transferId);
}