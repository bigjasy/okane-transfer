package ma.ensam.okanetransfer.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ma.ensam.okanetransfer.domain.user.Agent;
import ma.ensam.okanetransfer.domain.user.Client;
import ma.ensam.okanetransfer.domain.user.Manager;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.dto.dashboard.DashboardSummaryResponse;
import ma.ensam.okanetransfer.enums.TransferStatus;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.repository.CommissionRepository;
import ma.ensam.okanetransfer.repository.TransferRepository;
import ma.ensam.okanetransfer.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final String[] MONTH_LABELS = {
            "Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Aoû", "Sep", "Oct", "Nov", "Déc"
    };

    private final TransferRepository transferRepository;
    private final UserRepository userRepository;
    private final CommissionRepository commissionRepository;

    public DashboardService(
            TransferRepository transferRepository,
            UserRepository userRepository,
            CommissionRepository commissionRepository
    ) {
        this.transferRepository = transferRepository;
        this.userRepository = userRepository;
        this.commissionRepository = commissionRepository;
    }

    public DashboardSummaryResponse getAdminDashboard() {
        DashboardSummaryResponse response = new DashboardSummaryResponse();
        response.setTotalVolume(transferRepository.sumTotalVolume());
        response.setTotalFees(transferRepository.sumTotalFees());
        response.setTransferCount(transferRepository.count());
        response.setTotalCommissions(sumAllCommissions());
        response.setCharts(buildCharts(null));
        return response;
    }

    public DashboardSummaryResponse getManagerDashboard(String email) {
        Manager manager = (Manager) userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("Manager introuvable"));

        Long agencyId = manager.getAgency().getId();

        DashboardSummaryResponse response = new DashboardSummaryResponse();
        response.setTotalVolume(transferRepository.sumTotalVolumeByAgency(agencyId));
        response.setTotalFees(transferRepository.sumTotalFeesByAgency(agencyId));
        response.setTransferCount(transferRepository.countBySourceAgencyId(agencyId));
        response.setTotalCommissions(
                commissionRepository.sumAgencyCommission(agencyId).add(commissionRepository.sumCentralCommission(agencyId))
        );
        response.setCharts(buildCharts(agencyId));
        return response;
    }

    public DashboardSummaryResponse getAgentDashboard(String email) {
        Agent agent = (Agent) userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("Agent introuvable"));

        Long agentId = agent.getId();
        Long agencyId = agent.getAgency() != null ? agent.getAgency().getId() : null;

        DashboardSummaryResponse response = new DashboardSummaryResponse();
        response.setTotalVolume(transferRepository.sumTotalVolumeByAgent(agentId));
        response.setTotalFees(transferRepository.sumTotalFeesByAgent(agentId));
        response.setTransferCount(transferRepository.countByCreatedByAgentId(agentId));
        response.setTotalCommissions(agencyId == null
                ? BigDecimal.ZERO
                : commissionRepository.sumAgencyCommission(agencyId));
        response.setCharts(buildCharts(agencyId));
        return response;
    }

    public DashboardSummaryResponse getClientDashboard(String email) {
        Client client = (Client) userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("Client introuvable"));

        Long clientId = client.getId();

        DashboardSummaryResponse response = new DashboardSummaryResponse();
        response.setTotalVolume(transferRepository.sumTotalVolumeByClient(clientId));
        response.setTransferCount(transferRepository.countBySenderId(clientId));
        response.setCharts(buildCharts(null));
        return response;
    }

    private BigDecimal sumAllCommissions() {
        return commissionRepository.sumAgencyCommission(null).add(commissionRepository.sumCentralCommission(null));
    }

    private Map<String, Object> buildCharts(Long agencyId) {
        Map<String, Object> charts = new HashMap<>();

        List<String> statusLabels = new ArrayList<>();
        List<Long> statusCounts = new ArrayList<>();
        Map<String, Long> statusDistribution = new LinkedHashMap<>();
        for (TransferStatus status : TransferStatus.values()) {
            long count = agencyId == null
                    ? transferRepository.countByStatus(status)
                    : transferRepository.countBySourceAgencyIdAndStatus(agencyId, status);
            if (count > 0) {
                statusLabels.add(status.name());
                statusCounts.add(count);
                statusDistribution.put(status.name(), count);
            }
        }
        charts.put("statusLabels", statusLabels);
        charts.put("status", statusCounts);
        charts.put("statusDistribution", statusDistribution);

        LocalDateTime since = LocalDate.now().minusMonths(5).withDayOfMonth(1).atStartOfDay();
        List<Object[]> monthlyRows = agencyId == null
                ? transferRepository.sumVolumeByMonthSince(since)
                : transferRepository.sumVolumeByMonthSinceByAgency(since, agencyId);

        Map<String, BigDecimal> volumeByKey = new HashMap<>();
        for (Object[] row : monthlyRows) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            BigDecimal volume = row[2] instanceof BigDecimal amount ? amount : BigDecimal.valueOf(((Number) row[2]).doubleValue());
            volumeByKey.put(year + "-" + String.format("%02d", month), volume);
        }

        List<String> monthLabels = new ArrayList<>();
        List<Double> volumeByMonth = new ArrayList<>();
        List<Map<String, Object>> volumeHistory = new ArrayList<>();
        LocalDate cursor = LocalDate.now().minusMonths(5).withDayOfMonth(1);
        LocalDate end = LocalDate.now().withDayOfMonth(1);
        while (!cursor.isAfter(end)) {
            String key = cursor.getYear() + "-" + String.format("%02d", cursor.getMonthValue());
            BigDecimal volume = volumeByKey.getOrDefault(key, BigDecimal.ZERO);
            monthLabels.add(MONTH_LABELS[cursor.getMonthValue() - 1]);
            volumeByMonth.add(volume.doubleValue());
            Map<String, Object> point = new HashMap<>();
            point.put("month", key);
            point.put("volume", volume);
            volumeHistory.add(point);
            cursor = cursor.plusMonths(1);
        }

        charts.put("monthLabels", monthLabels);
        charts.put("volumeByMonth", volumeByMonth);
        charts.put("months", volumeByMonth);
        charts.put("volumeHistory", volumeHistory);
        return charts;
    }
}
