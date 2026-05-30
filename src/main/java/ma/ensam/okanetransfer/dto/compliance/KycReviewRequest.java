package ma.ensam.okanetransfer.dto.compliance;

import jakarta.validation.constraints.NotNull;
import ma.ensam.okanetransfer.enums.KycStatus;

public class KycReviewRequest {

    @NotNull(message = "KYC status is required")
    private KycStatus status;

    private String rejectionReason;

    public KycStatus getStatus() {
        return status;
    }

    public void setStatus(KycStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
