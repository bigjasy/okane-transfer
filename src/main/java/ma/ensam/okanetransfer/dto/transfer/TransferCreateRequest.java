package ma.ensam.okanetransfer.dto.transfer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import ma.ensam.okanetransfer.enums.TransferChannel;

public class TransferCreateRequest {
@NotNull(message = "L'ID de l'expéditeur est obligatoire")
    private Long senderClientId;
    
    @Valid // Déclenche la validation en cascade sur l'objet imbriqué
    @NotNull(message = "Les informations du bénéficiaire sont obligatoires")
    private BeneficiaryRequest beneficiary;
    
    @NotNull(message = "L'agence source est obligatoire")
    private Long sourceAgencyId;
    
    @NotNull(message = "L'agence de destination est obligatoire")
    private Long destinationAgencyId;
    
    @NotNull(message = "Le corridor est obligatoire")
    private Long corridorId;
    
    @NotBlank(message = "La devise source est obligatoire")
    private String sourceCurrency;
    
    @NotBlank(message = "La devise cible est obligatoire")
    private String targetCurrency;
    
    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant du transfert doit être strictement positif")
    private BigDecimal amount;
    
    @NotNull(message = "Le canal de transfert est obligatoire")
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