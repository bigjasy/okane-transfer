package ma.ensam.okanetransfer.dto.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class TransfersReportResponse {

    private String reportType = "TRANSFERS_SUMMARY";
    private String format;
    private LocalDateTime generatedAt;
    private long totalTransfers;
    private BigDecimal totalVolume;
    private BigDecimal totalFees;
    private Map<String, Long> transfersByStatus;

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

    public BigDecimal getTotalFees() {
        return totalFees;
    }

    public void setTotalFees(BigDecimal totalFees) {
        this.totalFees = totalFees;
    }

    public Map<String, Long> getTransfersByStatus() {
        return transfersByStatus;
    }

    public void setTransfersByStatus(Map<String, Long> transfersByStatus) {
        this.transfersByStatus = transfersByStatus;
    }
}
