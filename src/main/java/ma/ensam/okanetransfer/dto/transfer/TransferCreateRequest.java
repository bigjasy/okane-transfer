package ma.ensam.okanetransfer.dto.transfer;

import java.math.BigDecimal;
import ma.ensam.okanetransfer.enums.TransferChannel;

public class TransferCreateRequest {
    private Long senderClientId;
    private BeneficiaryRequest beneficiary;
    private Long sourceAgencyId;
    private Long destinationAgencyId;
    private Long corridorId;
    private String sourceCurrency;
    private String targetCurrency;
    private BigDecimal amount;
    private TransferChannel channel;

    // Getters and Setters
    public Long getSenderClientId() {
        return senderClientId;
    }
    public void setSenderClientId(Long senderClientId) {
        this.senderClientId = senderClientId;
    }
    public BeneficiaryRequest getBeneficiary() {
        return beneficiary;
    }
    public void setBeneficiary(BeneficiaryRequest beneficiary) {
        this.beneficiary = beneficiary;
    }
    public Long getSourceAgencyId() {
        return sourceAgencyId;
    }
    public void setSourceAgencyId(Long sourceAgencyId) {
        this.sourceAgencyId = sourceAgencyId;
    }
    public Long getDestinationAgencyId() {
        return destinationAgencyId;
    }
    public void setDestinationAgencyId(Long destinationAgencyId) {
        this.destinationAgencyId = destinationAgencyId;
    }
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
    public TransferChannel getChannel() {
        return channel;
    }
    public void setChannel(TransferChannel channel) {
        this.channel = channel;
    }
    
    
}