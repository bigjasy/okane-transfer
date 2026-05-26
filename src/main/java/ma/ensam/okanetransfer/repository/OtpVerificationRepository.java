package ma.ensam.okanetransfer.repository;

import java.util.Optional;
import ma.ensam.okanetransfer.domain.security.OtpVerification;
import ma.ensam.okanetransfer.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByUserIdAndPurposeOrderByCreatedAtDesc(Long userId, OtpPurpose purpose);
}
