package ma.ensam.okanetransfer.dto.compliance;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class AmlCheckTransferRequest {

    @NotBlank(message = "Transfer reference is required")
    private String transferReference;

    public String getTransferReference() {
        return transferReference;
    }

    public void setTransferReference(String transferReference) {
        this.transferReference = transferReference;
    }
}
