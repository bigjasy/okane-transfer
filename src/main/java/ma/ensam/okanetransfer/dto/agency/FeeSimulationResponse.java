package ma.ensam.okanetransfer.dto.agency;

import java.math.BigDecimal;

public class FeeSimulationResponse {
    private BigDecimal amount;
    private BigDecimal feeAmount;
    private BigDecimal totalToPay;
    private BigDecimal exchangeRate;
    private BigDecimal receivedAmount;
    private BigDecimal agencyCommission;
    private BigDecimal centralCommission;

    // Getters and Setters
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public BigDecimal getFeeAmount() {
        return feeAmount;
    }
    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }
    public BigDecimal getTotalToPay() {
        return totalToPay;
    }
    public void setTotalToPay(BigDecimal totalToPay) {
        this.totalToPay = totalToPay;
    }
    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }
    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }
    public BigDecimal getReceivedAmount() {
        return receivedAmount;
    }
    public void setReceivedAmount(BigDecimal receivedAmount) {
        this.receivedAmount = receivedAmount;
    }
    public BigDecimal getAgencyCommission() {
        return agencyCommission;
    }
    public void setAgencyCommission(BigDecimal agencyCommission) {
        this.agencyCommission = agencyCommission;
    }
    public BigDecimal getCentralCommission() {
        return centralCommission;
    }
    public void setCentralCommission(BigDecimal centralCommission) {
        this.centralCommission = centralCommission;
    }
    
    
}