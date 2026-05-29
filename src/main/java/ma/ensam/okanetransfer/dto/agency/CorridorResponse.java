package ma.ensam.okanetransfer.dto.agency;

import java.math.BigDecimal;
import ma.ensam.okanetransfer.dto.referential.CountryResponse; 

public class CorridorResponse {
    private Long id;
    private CountryResponse sourceCountry;
    private CountryResponse destinationCountry;
    private boolean active;
    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;

    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public CountryResponse getSourceCountry() {
        return sourceCountry;
    }
    public void setSourceCountry(CountryResponse sourceCountry) {
        this.sourceCountry = sourceCountry;
    }
    public CountryResponse getDestinationCountry() {
        return destinationCountry;
    }
    public void setDestinationCountry(CountryResponse destinationCountry) {
        this.destinationCountry = destinationCountry;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
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
    
    
}