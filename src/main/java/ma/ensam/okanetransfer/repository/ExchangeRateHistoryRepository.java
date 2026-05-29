package ma.ensam.okanetransfer.repository;

import java.util.List;
import ma.ensam.okanetransfer.domain.referential.ExchangeRateHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateHistoryRepository extends JpaRepository<ExchangeRateHistory, Long> {
    List<ExchangeRateHistory> findBySourceCurrencyCodeAndTargetCurrencyCodeOrderByChangedAtDesc(
            String sourceCurrencyCode,
            String targetCurrencyCode
    );
}
