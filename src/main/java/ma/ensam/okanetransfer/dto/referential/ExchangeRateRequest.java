package ma.ensam.okanetransfer.dto.referential;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import ma.ensam.okanetransfer.enums.RateSource;

public class ExchangeRateRequest {

    @NotNull(message = "Source currency id is required")
    private Long sourceCurrencyId;

    @NotNull(message = "Target currency id is required")
    private Long targetCurrencyId;

    @NotNull(message = "Rate is required")
    @Positive(message = "Rate must be positive")
    private BigDecimal rate;

    @NotNull(message = "Rate source is required")
    private RateSource source;

    public Long getSourceCurrencyId() {
        return sourceCurrencyId;
    }

    public void setSourceCurrencyId(Long sourceCurrencyId) {
        this.sourceCurrencyId = sourceCurrencyId;
    }

    public Long getTargetCurrencyId() {
        return targetCurrencyId;
    }

    public void setTargetCurrencyId(Long targetCurrencyId) {
        this.targetCurrencyId = targetCurrencyId;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public RateSource getSource() {
        return source;
    }

    public void setSource(RateSource source) {
        this.source = source;
    }
}
