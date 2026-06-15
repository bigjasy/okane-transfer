package ma.ensam.okanetransfer.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import ma.ensam.okanetransfer.dto.referential.ExternalRateQuote;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Simulates a third-party FX provider (e.g. OpenExchangeRates-style feed).
 * Rates vary slightly by day so sync operations produce realistic history entries.
 */
@Component
public class ExternalExchangeRateProvider {

    private final String providerName;

    public ExternalExchangeRateProvider(
            @Value("${exchange-rate.external.provider:OkaneFX-Simulated}") String providerName
    ) {
        this.providerName = providerName;
    }

    public String getProviderName() {
        return providerName;
    }

    public List<ExternalRateQuote> fetchLatestQuotes() {
        int dayOffset = LocalDate.now().getDayOfMonth();
        BigDecimal jitter = new BigDecimal("0.000" + (dayOffset % 7)).setScale(6, RoundingMode.HALF_UP);

        List<ExternalRateQuote> quotes = new ArrayList<>();
        quotes.add(new ExternalRateQuote("MAD", "EUR", baseRate("0.092").add(jitter)));
        quotes.add(new ExternalRateQuote("MAD", "XOF", baseRate("61.00").add(jitter.multiply(new BigDecimal("10")))));
        quotes.add(new ExternalRateQuote("MAD", "USD", baseRate("0.098").subtract(jitter)));
        quotes.add(new ExternalRateQuote("EUR", "MAD", baseRate("10.87").subtract(jitter.multiply(new BigDecimal("5")))));
        quotes.add(new ExternalRateQuote("EUR", "XOF", baseRate("655.957")));
        quotes.add(new ExternalRateQuote("USD", "MAD", baseRate("10.20").add(jitter.multiply(new BigDecimal("3")))));
        return quotes;
    }

    private BigDecimal baseRate(String value) {
        return new BigDecimal(value).setScale(6, RoundingMode.HALF_UP);
    }
}
