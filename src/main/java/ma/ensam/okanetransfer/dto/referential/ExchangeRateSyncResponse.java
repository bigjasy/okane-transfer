package ma.ensam.okanetransfer.dto.referential;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import ma.ensam.okanetransfer.enums.RateSource;

public class ExchangeRateSyncResponse {
    private String provider;
    private RateSource source;
    private LocalDateTime syncedAt;
    private int updatedCount;
    private List<ExchangeRateResponse> rates = new ArrayList<>();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public RateSource getSource() {
        return source;
    }

    public void setSource(RateSource source) {
        this.source = source;
    }

    public LocalDateTime getSyncedAt() {
        return syncedAt;
    }

    public void setSyncedAt(LocalDateTime syncedAt) {
        this.syncedAt = syncedAt;
    }

    public int getUpdatedCount() {
        return updatedCount;
    }

    public void setUpdatedCount(int updatedCount) {
        this.updatedCount = updatedCount;
    }

    public List<ExchangeRateResponse> getRates() {
        return rates;
    }

    public void setRates(List<ExchangeRateResponse> rates) {
        this.rates = rates;
    }
}
