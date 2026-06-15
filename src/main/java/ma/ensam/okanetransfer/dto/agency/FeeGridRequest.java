package ma.ensam.okanetransfer.dto.agency;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class FeeGridRequest {

    @NotNull(message = "Le corridor est obligatoire")
    private Long corridorId;

    @NotNull(message = "Le montant minimum est obligatoire")
    @DecimalMin("0.0")
    private BigDecimal minAmount;

    @NotNull(message = "Le montant maximum est obligatoire")
    private BigDecimal maxAmount;

    @NotNull(message = "Les frais fixes sont obligatoires")
    @DecimalMin("0.0")
    private BigDecimal fixedFee;

    @NotNull(message = "Le pourcentage de frais est obligatoire")
    @DecimalMin("0.0")
    private BigDecimal percentageFee;

    @NotNull(message = "La part agence est obligatoire")
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private BigDecimal agencyCommissionRate;

    @NotNull(message = "La part centrale est obligatoire")
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private BigDecimal centralCommissionRate;

    private LocalDate validFrom;
    private LocalDate validTo;
    private boolean active = true;

    // --- Getters et Setters ---
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
