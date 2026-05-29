package ma.ensam.okanetransfer.dto.agency;

import java.math.BigDecimal;

public class CorridorRequest {
    private Long sourceCountryId;
    private Long destinationCountryId;
    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;
    private boolean active;

    // Getters and Setters
    public Long getSourceCountryId() {
        return sourceCountryId;
    }
    public void setSourceCountryId(Long sourceCountryId) {
        this.sourceCountryId = sourceCountryId;
    }
    public Long getDestinationCountryId() {
        return destinationCountryId;
    }
    public void setDestinationCountryId(Long destinationCountryId) {
        this.destinationCountryId = destinationCountryId;
    }
    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }
    public void setDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }
    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }
    public void setMonthlyLimit(BigDecimal monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    
    
}