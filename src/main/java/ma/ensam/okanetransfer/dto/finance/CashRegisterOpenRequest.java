package ma.ensam.okanetransfer.dto.finance;

import java.math.BigDecimal;

public class CashRegisterOpenRequest {
    private Long agentId;
    private Long agencyId;
    private String currencyCode;
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