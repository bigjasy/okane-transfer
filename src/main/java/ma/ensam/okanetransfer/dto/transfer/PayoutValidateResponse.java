package ma.ensam.okanetransfer.dto.transfer;

public class PayoutValidateResponse {
    private boolean valid;
    private boolean requiresOtp;
    private String message;

    public PayoutValidateResponse() {
    }

    public PayoutValidateResponse(boolean valid, boolean requiresOtp, String message) {
        this.valid = valid;
        this.requiresOtp = requiresOtp;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isRequiresOtp() {
        return requiresOtp;
    }

    public void setRequiresOtp(boolean requiresOtp) {
        this.requiresOtp = requiresOtp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
