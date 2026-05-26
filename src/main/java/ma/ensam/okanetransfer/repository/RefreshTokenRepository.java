package ma.ensam.okanetransfer.repository;

import java.util.List;
import java.util.Optional;
import ma.ensam.okanetransfer.domain.security.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByUserIdAndRevokedFalse(Long userId);
}
