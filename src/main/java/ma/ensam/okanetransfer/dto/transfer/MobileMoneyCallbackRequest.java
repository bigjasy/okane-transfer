package ma.ensam.okanetransfer.dto.transfer;

import ma.ensam.okanetransfer.enums.MobileMoneyStatus;

public class MobileMoneyCallbackRequest {

    private MobileMoneyStatus status = MobileMoneyStatus.CONFIRMED;
    private String operatorTransactionReference;

    public MobileMoneyStatus getStatus() {
        return status;
    }

    public void setStatus(MobileMoneyStatus status) {
        this.status = status;
    }

    public String getOperatorTransactionReference() {
        return operatorTransactionReference;
    }

    public void setOperatorTransactionReference(String operatorTransactionReference) {
        this.operatorTransactionReference = operatorTransactionReference;
    }
}
