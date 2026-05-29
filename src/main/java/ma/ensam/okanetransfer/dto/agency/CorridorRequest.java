package ma.ensam.okanetransfer.dto.agency;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CorridorRequest {
@NotNull(message = "Le pays source est obligatoire")
    private Long sourceCountryId;
    
    @NotNull(message = "Le pays de destination est obligatoire")
    private Long destinationCountryId;
    
    @NotNull(message = "La limite journalière est obligatoire")
    @Positive(message = "La limite journalière doit être positive")
    private BigDecimal dailyLimit;
    
    @NotNull(message = "La limite mensuelle est obligatoire")
    @Positive(message = "La limite mensuelle doit être positive")
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