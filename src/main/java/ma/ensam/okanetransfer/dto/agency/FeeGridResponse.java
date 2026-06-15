package ma.ensam.okanetransfer.dto.agency;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FeeGridResponse {
    private Long id;
    private Long corridorId;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal fixedFee;
    private BigDecimal percentageFee;
    private BigDecimal agencyCommissionRate;
    private BigDecimal centralCommissionRate;
    private LocalDate validFrom;
    private LocalDate validTo;
    private boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCorridorId() { return corridorId; }
    public void setCorridorId(Long corridorId) { this.corridorId = corridorId; }
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }
    public BigDecimal getFixedFee() { return fixedFee; }
    public void setFixedFee(BigDecimal fixedFee) { this.fixedFee = fixedFee; }
    public BigDecimal getPercentageFee() { return percentageFee; }
    public void setPercentageFee(BigDecimal percentageFee) { this.percentageFee = percentageFee; }
    public BigDecimal getAgencyCommissionRate() { return agencyCommissionRate; }
    public void setAgencyCommissionRate(BigDecimal agencyCommissionRate) { this.agencyCommissionRate = agencyCommissionRate; }
    public BigDecimal getCentralCommissionRate() { return centralCommissionRate; }
    public void setCentralCommissionRate(BigDecimal centralCommissionRate) { this.centralCommissionRate = centralCommissionRate; }
    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }
    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}