package ma.ensam.okanetransfer.domain.transfer;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import ma.ensam.okanetransfer.domain.user.Agent;
import ma.ensam.okanetransfer.domain.agency.Agency;
import ma.ensam.okanetransfer.enums.IdentityType;
import ma.ensam.okanetransfer.util.AesEncryptionConverter;

@Entity
@Table(name = "transfer_payments")
public class TransferPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", nullable = false, unique = true)
    private Transfer transfer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by_agent_id", nullable = false)
    private Agent paidByAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_at_agency_id", nullable = false)
    private Agency paidAtAgency;

    @Enumerated(EnumType.STRING)
    @Column(name = "beneficiary_identity_type", nullable = false)
    private IdentityType beneficiaryIdentityType;

    @Convert(converter = AesEncryptionConverter.class)
    @Column(name = "beneficiary_identity_number_encrypted", nullable = false)
    private String beneficiaryIdentityNumberEncrypted;

    @Column(name = "paid_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "paid_at", nullable = false, updatable = false)
    private LocalDateTime paidAt;

    @PrePersist
    protected void onPay() {
        this.paidAt = LocalDateTime.now();
    }

    // Constructeur
    public TransferPayment() {}

    // Getters et Setters
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

    public Agent getPaidByAgent() {
        return paidByAgent;
    }

    public void setPaidByAgent(Agent paidByAgent) {
        this.paidByAgent = paidByAgent;
    }

    public Agency getPaidAtAgency() {
        return paidAtAgency;
    }

    public void setPaidAtAgency(Agency paidAtAgency) {
        this.paidAtAgency = paidAtAgency;
    }

    public IdentityType getBeneficiaryIdentityType() {
        return beneficiaryIdentityType;
    }

    public void setBeneficiaryIdentityType(IdentityType beneficiaryIdentityType) {
        this.beneficiaryIdentityType = beneficiaryIdentityType;
    }

    public String getBeneficiaryIdentityNumberEncrypted() {
        return beneficiaryIdentityNumberEncrypted;
    }

    public void setBeneficiaryIdentityNumberEncrypted(String beneficiaryIdentityNumberEncrypted) {
        this.beneficiaryIdentityNumberEncrypted = beneficiaryIdentityNumberEncrypted;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    


}