package ma.ensam.okanetransfer.dto.transfer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ma.ensam.okanetransfer.enums.IdentityType;

public class PayoutConfirmRequest {
    @NotBlank(message = "La référence du transfert est obligatoire")
    private String transferReference;
    
    @NotBlank(message = "Le code de retrait est obligatoire")
    private String withdrawalCode;
    
    @NotNull(message = "Le type de pièce d'identité est obligatoire")
    private IdentityType identityType;
    
    @NotBlank(message = "Le numéro de pièce d'identité est obligatoire")
    private String identityNumber;
    
    @NotBlank(message = "Le code OTP est obligatoire")
    private String otpCode;

    // Getters and Setters
    public String getTransferReference() {
        return transferReference;
    }
    public void setTransferReference(String transferReference) {
        this.transferReference = transferReference;
    }
    public String getWithdrawalCode() {
        return withdrawalCode;
    }
    public void setWithdrawalCode(String withdrawalCode) {
        this.withdrawalCode = withdrawalCode;
    }
    public IdentityType getIdentityType() {
        return identityType;
    }
    public void setIdentityType(IdentityType identityType) {
        this.identityType = identityType;
    }
    public String getIdentityNumber() {
        return identityNumber;
    }
    public void setIdentityNumber(String identityNumber) {
        this.identityNumber = identityNumber;
    }
    public String getOtpCode() {
        return otpCode;
    }
    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
    
    
}