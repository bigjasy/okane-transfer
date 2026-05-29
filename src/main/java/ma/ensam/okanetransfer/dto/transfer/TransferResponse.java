package ma.ensam.okanetransfer.dto.transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import ma.ensam.okanetransfer.enums.TransferStatus;

public class TransferResponse {
    private Long id;
    private String reference;
    private TransferStatus status;
    private String senderName;
    private String beneficiaryName;
    private BigDecimal sentAmount;
    private BigDecimal feeAmount;
    private BigDecimal receivedAmount;
    private String sourceCurrency;
    private String targetCurrency;
    private BigDecimal exchangeRateApplied;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getReference() {
        return reference;
    }
    public void setReference(String reference) {
        this.reference = reference;
    }
    public TransferStatus getStatus() {
        return status;
    }
    public void setStatus(TransferStatus status) {
        this.status = status;
    }
    public String getSenderName() {
        return senderName;
    }
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    public String getBeneficiaryName() {
        return beneficiaryName;
    }
    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }
    public BigDecimal getSentAmount() {
        return sentAmount;
    }
    public void setSentAmount(BigDecimal sentAmount) {
        this.sentAmount = sentAmount;
    }
    public BigDecimal getFeeAmount() {
        return feeAmount;
    }
    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }
    public BigDecimal getReceivedAmount() {
        return receivedAmount;
    }
    public void setReceivedAmount(BigDecimal receivedAmount) {
        this.receivedAmount = receivedAmount;
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
    public BigDecimal getExchangeRateApplied() {
        return exchangeRateApplied;
    }
    public void setExchangeRateApplied(BigDecimal exchangeRateApplied) {
        this.exchangeRateApplied = exchangeRateApplied;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    
}