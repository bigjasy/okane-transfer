package ma.ensam.okanetransfer.dto.transfer;

public class MobileMoneyReconciliationResponse {

    private int reconciled;
    private int mismatches;

    public MobileMoneyReconciliationResponse() {
    }

    public MobileMoneyReconciliationResponse(int reconciled, int mismatches) {
        this.reconciled = reconciled;
        this.mismatches = mismatches;
    }

    public int getReconciled() {
        return reconciled;
    }

    public void setReconciled(int reconciled) {
        this.reconciled = reconciled;
    }

    public int getMismatches() {
        return mismatches;
    }

    public void setMismatches(int mismatches) {
        this.mismatches = mismatches;
    }
}
