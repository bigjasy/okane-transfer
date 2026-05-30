package ma.ensam.okanetransfer.dto.compliance;

import jakarta.validation.constraints.NotNull;
import ma.ensam.okanetransfer.enums.AmlAlertStatus;

public class AmlReviewRequest {

    @NotNull(message = "Alert status is required")
    private AmlAlertStatus status;

    private String comment;

    public AmlAlertStatus getStatus() {
        return status;
    }

    public void setStatus(AmlAlertStatus status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
