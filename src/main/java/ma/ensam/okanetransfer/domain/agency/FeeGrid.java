package ma.ensam.okanetransfer.domain.agency;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import ma.ensam.okanetransfer.domain.referential.Currency;

@Entity
@Table(name = "fee_grids")
public class FeeGrid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corridor_id", nullable = false)
    private Corridor corridor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_currency_id", nullable = false)
    private Currency sourceCurrency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_currency_id", nullable = false)
    private Currency targetCurrency;

    @Column(name = "min_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "fixed_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal fixedFee;

    @Column(name = "percentage_fee", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentageFee;

    @Column(name = "agency_commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal agencyCommissionRate;

    @Column(name = "central_commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal centralCommissionRate;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(nullable = false)
    private boolean active;

    // Constructeur
    public FeeGrid() {}

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Corridor getCorridor() {
        return corridor;
    }

    public void setCorridor(Corridor corridor) {
        this.corridor = corridor;
    }

    public Currency getSourceCurrency() {
        return sourceCurrency;
    }

    public void setSourceCurrency(Currency sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }

    public Currency getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(Currency targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public BigDecimal getFixedFee() {
        return fixedFee;
    }

    public void setFixedFee(BigDecimal fixedFee) {
        this.fixedFee = fixedFee;
    }

    public BigDecimal getPercentageFee() {
        return percentageFee;
    }

    public void setPercentageFee(BigDecimal percentageFee) {
        this.percentageFee = percentageFee;
    }

    public BigDecimal getAgencyCommissionRate() {
        return agencyCommissionRate;
    }

    public void setAgencyCommissionRate(BigDecimal agencyCommissionRate) {
        this.agencyCommissionRate = agencyCommissionRate;
    }

    public BigDecimal getCentralCommissionRate() {
        return centralCommissionRate;
    }

    public void setCentralCommissionRate(BigDecimal centralCommissionRate) {
        this.centralCommissionRate = centralCommissionRate;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
