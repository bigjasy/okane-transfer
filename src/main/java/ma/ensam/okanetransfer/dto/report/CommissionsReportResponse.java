package ma.ensam.okanetransfer.dto.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CommissionsReportResponse {

    private String reportType = "COMMISSIONS";
    private String format;
    private Long agencyId;
    private LocalDateTime generatedAt;
    private BigDecimal totalAgencyCommissions;
    private BigDecimal totalCentralCommissions;

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Long getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(Long agencyId) {
        this.agencyId = agencyId;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public BigDecimal getTotalAgencyCommissions() {
        return totalAgencyCommissions;
    }

    public void setTotalAgencyCommissions(BigDecimal totalAgencyCommissions) {
        this.totalAgencyCommissions = totalAgencyCommissions;
    }

    public BigDecimal getTotalCentralCommissions() {
        return totalCentralCommissions;
    }

    public void setTotalCentralCommissions(BigDecimal totalCentralCommissions) {
        this.totalCentralCommissions = totalCentralCommissions;
    }
}
