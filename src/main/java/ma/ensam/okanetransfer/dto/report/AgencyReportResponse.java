package ma.ensam.okanetransfer.dto.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AgencyReportResponse {

    private Long agencyId;
    private String agencyName;
    private String agencyCode;
    private String format;
    private LocalDateTime generatedAt;
    private long totalTransfers;
    private BigDecimal totalVolume;
    private BigDecimal totalAgencyCommissions;

    public Long getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(Long agencyId) {
        this.agencyId = agencyId;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }

    public String getAgencyCode() {
        return agencyCode;
    }

    public void setAgencyCode(String agencyCode) {
        this.agencyCode = agencyCode;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public long getTotalTransfers() {
        return totalTransfers;
    }

    public void setTotalTransfers(long totalTransfers) {
        this.totalTransfers = totalTransfers;
    }

    public BigDecimal getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(BigDecimal totalVolume) {
        this.totalVolume = totalVolume;
    }

    public BigDecimal getTotalAgencyCommissions() {
        return totalAgencyCommissions;
    }

    public void setTotalAgencyCommissions(BigDecimal totalAgencyCommissions) {
        this.totalAgencyCommissions = totalAgencyCommissions;
    }
}
