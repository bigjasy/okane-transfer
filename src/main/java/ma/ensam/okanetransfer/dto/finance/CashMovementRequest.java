package ma.ensam.okanetransfer.dto.finance;

import java.math.BigDecimal;
import ma.ensam.okanetransfer.enums.CashMovementType;

public class CashMovementRequest {
    private CashMovementType type;
    private BigDecimal amount;
    private String currencyCode;
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