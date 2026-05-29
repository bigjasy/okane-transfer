package ma.ensam.okanetransfer.repository;

import java.util.List;
import java.util.Optional;
import ma.ensam.okanetransfer.domain.referential.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Optional<Currency> findByCode(String code);

    List<Currency> findByActiveTrue();
}
