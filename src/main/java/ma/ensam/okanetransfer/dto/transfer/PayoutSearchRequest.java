package ma.ensam.okanetransfer.dto.transfer;

public class PayoutSearchRequest {
    private String withdrawalCode;
    private String beneficiaryPhoneNumber;

    // Getters and Setters
    public String getWithdrawalCode() {
        return withdrawalCode;
    }
    public void setWithdrawalCode(String withdrawalCode) {
        this.withdrawalCode = withdrawalCode;
    }
    public String getBeneficiaryPhoneNumber() {
        return beneficiaryPhoneNumber;
    }
    public void setBeneficiaryPhoneNumber(String beneficiaryPhoneNumber) {
        this.beneficiaryPhoneNumber = beneficiaryPhoneNumber;
    }  
}