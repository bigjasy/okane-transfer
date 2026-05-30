package ma.ensam.okanetransfer.dto.compliance;

import java.time.LocalDateTime;
import ma.ensam.okanetransfer.enums.AmlAlertStatus;
import ma.ensam.okanetransfer.enums.AmlAlertType;
import ma.ensam.okanetransfer.enums.RiskLevel;

public class AmlAlertResponse {
    private Long id;
    private String transferReference;
    private AmlAlertType type;
    private RiskLevel riskLevel;
    private AmlAlertStatus status;
    private String description;
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

    public AmlAlertType getType() {
        return type;
    }

    public void setType(AmlAlertType type) {
        this.type = type;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public AmlAlertStatus getStatus() {
        return status;
    }

    public void setStatus(AmlAlertStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
