package ma.ensam.okanetransfer.dto.transfer;

import ma.ensam.okanetransfer.enums.IdentityType;

public class PayoutConfirmRequest {
    private String transferReference;
    private String withdrawalCode;
    private IdentityType identityType;
    private String identityNumber;
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