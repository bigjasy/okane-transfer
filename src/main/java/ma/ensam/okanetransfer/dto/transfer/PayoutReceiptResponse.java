package ma.ensam.okanetransfer.dto.transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import ma.ensam.okanetransfer.enums.TransferStatus;

public class PayoutReceiptResponse {

    private String transferReference;
    private String beneficiaryName;
    private BigDecimal paidAmount;
    private String currency;
    private LocalDateTime paidAt;
    private TransferStatus status;
    private String agentName;
    private String agencyName;
    private String maskedIdentityNumber;

    public String getTransferReference() {
        return transferReference;
    }

    public void setTransferReference(String transferReference) {
        this.transferReference = transferReference;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
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

    public TransferStatus getStatus() {
        return status;
    }

    public void setStatus(TransferStatus status) {
        this.status = status;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }

    public String getMaskedIdentityNumber() {
        return maskedIdentityNumber;
    }

    public void setMaskedIdentityNumber(String maskedIdentityNumber) {
        this.maskedIdentityNumber = maskedIdentityNumber;
    }
}
