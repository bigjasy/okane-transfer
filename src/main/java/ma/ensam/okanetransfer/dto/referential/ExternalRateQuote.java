package ma.ensam.okanetransfer.dto.referential;

import java.math.BigDecimal;

public class ExternalRateQuote {
    private String sourceCurrencyCode;
    private String targetCurrencyCode;
    private BigDecimal rate;

    public ExternalRateQuote() {
    }

    public ExternalRateQuote(String sourceCurrencyCode, String targetCurrencyCode, BigDecimal rate) {
        this.sourceCurrencyCode = sourceCurrencyCode;
        this.targetCurrencyCode = targetCurrencyCode;
        this.rate = rate;
    }

    public String getSourceCurrencyCode() {
        return sourceCurrencyCode;
    }

    public void setSourceCurrencyCode(String sourceCurrencyCode) {
        this.sourceCurrencyCode = sourceCurrencyCode;
    }

    public String getTargetCurrencyCode() {
        return targetCurrencyCode;
    }

    public void setTargetCurrencyCode(String targetCurrencyCode) {
        this.targetCurrencyCode = targetCurrencyCode;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
}
