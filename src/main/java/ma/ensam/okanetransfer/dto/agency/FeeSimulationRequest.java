package ma.ensam.okanetransfer.dto.agency;

import java.math.BigDecimal;

public class FeeSimulationRequest {
    private Long corridorId;
    private String sourceCurrency;
    private String targetCurrency;
    private BigDecimal amount;

    // Getters and Setters
    public Long getCorridorId() {
        return corridorId;
    }
    public void setCorridorId(Long corridorId) {
        this.corridorId = corridorId;
    }
    public String getSourceCurrency() {
        return sourceCurrency;
    }
    public void setSourceCurrency(String sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }
    public String getTargetCurrency() {
        return targetCurrency;
    }
    public void setTargetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    
}