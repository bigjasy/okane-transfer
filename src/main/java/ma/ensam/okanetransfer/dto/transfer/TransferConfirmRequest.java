package ma.ensam.okanetransfer.dto.transfer;

public class TransferConfirmRequest {
    private String otpCode;

    // Getters and Setters
    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
}