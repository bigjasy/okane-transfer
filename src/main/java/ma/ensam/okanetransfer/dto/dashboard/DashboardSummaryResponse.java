package ma.ensam.okanetransfer.dto.dashboard;

import java.math.BigDecimal;
import java.util.Map;

public class DashboardSummaryResponse {

    private BigDecimal totalVolume = BigDecimal.ZERO;
    private long transferCount = 0;
    private BigDecimal totalFees = BigDecimal.ZERO;
    private BigDecimal totalCommissions = BigDecimal.ZERO;
    private Map<String, Object> charts;

    // Getters et Setters
    public BigDecimal getTotalVolume() { return totalVolume; }
    public void setTotalVolume(BigDecimal totalVolume) { this.totalVolume = totalVolume; }

    public long getTransferCount() { return transferCount; }
    public void setTransferCount(long transferCount) { this.transferCount = transferCount; }

    public BigDecimal getTotalFees() { return totalFees; }
    public void setTotalFees(BigDecimal totalFees) { this.totalFees = totalFees; }

    public BigDecimal getTotalCommissions() { return totalCommissions; }
    public void setTotalCommissions(BigDecimal totalCommissions) { this.totalCommissions = totalCommissions; }

    public Map<String, Object> getCharts() { return charts; }
    public void setCharts(Map<String, Object> charts) { this.charts = charts; }
}