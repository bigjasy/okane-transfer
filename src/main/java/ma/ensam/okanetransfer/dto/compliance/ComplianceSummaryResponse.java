package ma.ensam.okanetransfer.dto.compliance;

public class ComplianceSummaryResponse {
    private long pendingKycDocuments;
    private long openAmlAlerts;
    private long criticalAmlAlerts;
    private long activeWatchlistEntries;
    private long blockedTransfers;

    public long getPendingKycDocuments() {
        return pendingKycDocuments;
    }

    public void setPendingKycDocuments(long pendingKycDocuments) {
        this.pendingKycDocuments = pendingKycDocuments;
    }

    public long getOpenAmlAlerts() {
        return openAmlAlerts;
    }

    public void setOpenAmlAlerts(long openAmlAlerts) {
        this.openAmlAlerts = openAmlAlerts;
    }

    public long getCriticalAmlAlerts() {
        return criticalAmlAlerts;
    }

    public void setCriticalAmlAlerts(long criticalAmlAlerts) {
        this.criticalAmlAlerts = criticalAmlAlerts;
    }

    public long getActiveWatchlistEntries() {
        return activeWatchlistEntries;
    }

    public void setActiveWatchlistEntries(long activeWatchlistEntries) {
        this.activeWatchlistEntries = activeWatchlistEntries;
    }

    public long getBlockedTransfers() {
        return blockedTransfers;
    }

    public void setBlockedTransfers(long blockedTransfers) {
        this.blockedTransfers = blockedTransfers;
    }
}
