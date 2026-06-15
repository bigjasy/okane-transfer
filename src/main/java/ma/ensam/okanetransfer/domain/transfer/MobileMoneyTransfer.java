package ma.ensam.okanetransfer.domain.transfer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import ma.ensam.okanetransfer.enums.MobileMoneyOperator;
import ma.ensam.okanetransfer.enums.MobileMoneyStatus;
import ma.ensam.okanetransfer.enums.ReconciliationStatus;

@Entity
@Table(name = "mobile_money_transfers")
public class MobileMoneyTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", nullable = false, unique = true)
    private Transfer transfer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MobileMoneyOperator operator;

    @Column(name = "wallet_phone_number", length = 20)
    private String walletPhoneNumber;

    @Column(name = "operator_transaction_reference", length = 255)
    private String operatorTransactionReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MobileMoneyStatus status = MobileMoneyStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "reconciliation_status", nullable = false, length = 20)
    private ReconciliationStatus reconciliationStatus = ReconciliationStatus.NOT_RECONCILED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "reconciled_at")
    private LocalDateTime reconciledAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Transfer getTransfer() {
        return transfer;
    }

    public void setTransfer(Transfer transfer) {
        this.transfer = transfer;
    }

    public MobileMoneyOperator getOperator() {
        return operator;
    }

    public void setOperator(MobileMoneyOperator operator) {
        this.operator = operator;
    }

    public String getWalletPhoneNumber() {
        return walletPhoneNumber;
    }

    public void setWalletPhoneNumber(String walletPhoneNumber) {
        this.walletPhoneNumber = walletPhoneNumber;
    }

    public String getOperatorTransactionReference() {
        return operatorTransactionReference;
    }

    public void setOperatorTransactionReference(String operatorTransactionReference) {
        this.operatorTransactionReference = operatorTransactionReference;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReconciledAt() {
        return reconciledAt;
    }

    public void setReconciledAt(LocalDateTime reconciledAt) {
        this.reconciledAt = reconciledAt;
    }
}
