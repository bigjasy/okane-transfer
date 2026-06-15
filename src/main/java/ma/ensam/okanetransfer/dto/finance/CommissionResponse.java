package ma.ensam.okanetransfer.dto.finance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CommissionResponse {
    private Long id;
    private String transferReference;
    private BigDecimal agencyPart;
    private BigDecimal centralPart;
    private String currency;
    private String agencyName;
    private LocalDateTime createdAt;

    // Getters and Setters
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
    public BigDecimal getAgencyPart() {
        return agencyPart;
    }
    public void setAgencyPart(BigDecimal agencyPart) {
        this.agencyPart = agencyPart;
    }
    public BigDecimal getCentralPart() {
        return centralPart;
    }
    public void setCentralPart(BigDecimal centralPart) {
        this.centralPart = centralPart;
    }
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    
}