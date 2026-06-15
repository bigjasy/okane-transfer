package ma.ensam.okanetransfer.repository;

import java.util.List;
import ma.ensam.okanetransfer.domain.compliance.KycDocument;
import ma.ensam.okanetransfer.enums.KycStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {
    List<KycDocument> findByUserId(Long userId);

    Page<KycDocument> findByStatus(KycStatus status, Pageable pageable);

    long countByStatus(KycStatus status);
}
