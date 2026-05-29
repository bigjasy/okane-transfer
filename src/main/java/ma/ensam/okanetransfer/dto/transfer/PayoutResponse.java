package ma.ensam.okanetransfer.dto.transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import ma.ensam.okanetransfer.enums.TransferStatus;

public class PayoutResponse {
    private String transferReference;
    private TransferStatus status;
    private BigDecimal paidAmount;
    private String currency;
    private LocalDateTime paidAt;

    // Getters and Setters
    public String getTransferReference() {
        return transferReference;
    }
    public void setTransferReference(String transferReference) {
        this.transferReference = transferReference;
    }
    public TransferStatus getStatus() {
        return status;
    }
    public void setStatus(TransferStatus status) {
        this.status = status;
    }
    public BigDecimal getPaidAmount() {
        return paidAmount;
    }
    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    public LocalDateTime getPaidAt() {
        return paidAt;
    }
    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
    
    
}