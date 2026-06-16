package ma.ensam.okanetransfer.dto.agency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class FeeSimulationRequest {
    private Long corridorId;

    @NotBlank(message = "La devise source est obligatoire")
    private String sourceCurrency;

    @NotBlank(message = "La devise cible est obligatoire")
    private String targetCurrency;

    @NotNull(message = "Le montant à simuler est obligatoire")
    @Positive(message = "Le montant à simuler doit être strictement positif")
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