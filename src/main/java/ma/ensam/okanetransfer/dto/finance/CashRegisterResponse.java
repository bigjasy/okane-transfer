package ma.ensam.okanetransfer.dto.finance;

import java.math.BigDecimal;
import ma.ensam.okanetransfer.enums.CashRegisterStatus;

public class CashRegisterResponse {
    private Long id;
    private String agencyCode;
    private String agentName;
    private String currencyCode;
    private BigDecimal openingBalance;
    private BigDecimal currentBalance;
    private CashRegisterStatus status;

    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getAgencyCode() {
        return agencyCode;
    }
    public void setAgencyCode(String agencyCode) {
        this.agencyCode = agencyCode;
    }
    public String getAgentName() {
        return agentName;
    }
    public void setAgentName(String agentName) {
        this.agentName = agentName;
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
    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }
    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }
    public CashRegisterStatus getStatus() {
        return status;
    }
    public void setStatus(CashRegisterStatus status) {
        this.status = status;
    }
    
    
}