package ma.ensam.okanetransfer.dto.transfer;

import java.time.LocalDateTime;
import ma.ensam.okanetransfer.enums.MobileMoneyOperator;
import ma.ensam.okanetransfer.enums.MobileMoneyStatus;
import ma.ensam.okanetransfer.enums.ReconciliationStatus;

public class MobileMoneyResponse {

    private Long id;
    private String transferReference;
    private MobileMoneyOperator operator;
    private MobileMoneyStatus status;
    private ReconciliationStatus reconciliationStatus;
    private String operatorTransactionReference;
    private String walletPhoneNumber;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransferReference() {
        return transferReference;
    }

    public void setTransferReference(String transferReference) {
        this.transferReference = transferReference;
    }

    public MobileMoneyOperator getOperator() {
        return operator;
    }

    public void setOperator(MobileMoneyOperator operator) {
        this.operator = operator;
    }

    public MobileMoneyStatus getStatus() {
        return status;
    }

    public void setStatus(MobileMoneyStatus status) {
        this.status = status;
    }

    public ReconciliationStatus getReconciliationStatus() {
        return reconciliationStatus;
    }

    public void setReconciliationStatus(ReconciliationStatus reconciliationStatus) {
        this.reconciliationStatus = reconciliationStatus;
    }

    public String getOperatorTransactionReference() {
        return operatorTransactionReference;
    }

    public void setOperatorTransactionReference(String operatorTransactionReference) {
        this.operatorTransactionReference = operatorTransactionReference;
    }

    public String getWalletPhoneNumber() {
        return walletPhoneNumber;
    }

    public void setWalletPhoneNumber(String walletPhoneNumber) {
        this.walletPhoneNumber = walletPhoneNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
