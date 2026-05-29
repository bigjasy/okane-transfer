package ma.ensam.okanetransfer.dto.finance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public class CashRegisterOpenRequest {
    @NotNull(message = "L'ID de l'agent est obligatoire")
    private Long agentId;
    
    @NotNull(message = "L'ID de l'agence est obligatoire")
    private Long agencyId;
    
    @NotBlank(message = "La devise est obligatoire")
    private String currencyCode;
    
    @NotNull(message = "Le solde d'ouverture est obligatoire")
    @PositiveOrZero(message = "Le solde d'ouverture ne peut pas être négatif")
    private BigDecimal openingBalance;

    // Getters and Setters
    public Long getAgentId() {
        return agentId;
    }
    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }
    public Long getAgencyId() {
        return agencyId;
    }
    public void setAgencyId(Long agencyId) {
        this.agencyId = agencyId;
    }
    public String getCurrencyCode() {
        return currencyCode;
    }
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }
    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
    }
    
    
}