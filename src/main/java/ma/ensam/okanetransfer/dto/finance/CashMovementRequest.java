package ma.ensam.okanetransfer.dto.finance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import ma.ensam.okanetransfer.enums.CashMovementType;

public class CashMovementRequest {
    
    @NotNull(message = "Le type de mouvement est obligatoire")
    private CashMovementType type;
    
    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant du mouvement doit être strictement positif")
    private BigDecimal amount;
    
    @NotBlank(message = "Le code de la devise est obligatoire")
    private String currencyCode;
    
    @NotBlank(message = "Le motif du mouvement est obligatoire pour la traçabilité")
    private String reason;
    
    private Long transferId;

    // Getters and Setters
    public CashMovementType getType() {
        return type;
    }
    public void setType(CashMovementType type) {
        this.type = type;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCurrencyCode() {
        return currencyCode;
    }
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
    
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public Long getTransferId() {
        return transferId;
    }
    public void setTransferId(Long transferId) {
        this.transferId = transferId;
    }
}