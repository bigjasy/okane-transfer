package ma.ensam.okanetransfer.dto.compliance;

import java.util.List;

public class AmlCheckTransferResponse {
    private boolean blocked;
    private List<AmlAlertResponse> alerts;

    public AmlCheckTransferResponse() {
    }

    public AmlCheckTransferResponse(boolean blocked, List<AmlAlertResponse> alerts) {
        this.blocked = blocked;
        this.alerts = alerts;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public List<AmlAlertResponse> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<AmlAlertResponse> alerts) {
        this.alerts = alerts;
    }
}
