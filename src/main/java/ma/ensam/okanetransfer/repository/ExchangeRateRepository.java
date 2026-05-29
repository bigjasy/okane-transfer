package ma.ensam.okanetransfer.repository;

import java.util.List;
import java.util.Optional;
import ma.ensam.okanetransfer.domain.referential.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    @Query("SELECT e FROM ExchangeRate e WHERE e.sourceCurrency.code = :sourceCode "
            + "AND e.targetCurrency.code = :targetCode AND e.active = true")
    Optional<ExchangeRate> findActiveRate(
            @Param("sourceCode") String sourceCode,
            @Param("targetCode") String targetCode
    );

    List<ExchangeRate> findByActiveTrue();
}
