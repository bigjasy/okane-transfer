package ma.ensam.okanetransfer.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import ma.ensam.okanetransfer.domain.agency.Agency;
import ma.ensam.okanetransfer.domain.user.Manager;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.dto.report.AgencyReportResponse;
import ma.ensam.okanetransfer.dto.report.CommissionsReportResponse;
import ma.ensam.okanetransfer.dto.report.TransfersReportResponse;
import ma.ensam.okanetransfer.enums.Role;
import ma.ensam.okanetransfer.enums.TransferStatus;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.exception.ForbiddenOperationException;
import ma.ensam.okanetransfer.exception.ResourceNotFoundException;
import ma.ensam.okanetransfer.repository.AgencyRepository;
import ma.ensam.okanetransfer.repository.CommissionRepository;
import ma.ensam.okanetransfer.repository.TransferRepository;
import ma.ensam.okanetransfer.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final TransferRepository transferRepository;
    private final CommissionRepository commissionRepository;
    private final AgencyRepository agencyRepository;
    private final UserRepository userRepository;

    public ReportService(
            TransferRepository transferRepository,
            CommissionRepository commissionRepository,
            AgencyRepository agencyRepository,
            UserRepository userRepository
    ) {
        this.transferRepository = transferRepository;
        this.commissionRepository = commissionRepository;
        this.agencyRepository = agencyRepository;
        this.userRepository = userRepository;
    }

    public TransfersReportResponse getTransfersReport(String userEmail, String format, Long agencyId) {
        User user = requireUser(userEmail);
        Long scopedAgencyId = resolveAgencyScope(user, agencyId);

        TransfersReportResponse response = new TransfersReportResponse();
        response.setFormat(normalizeFormat(format));
        response.setGeneratedAt(LocalDateTime.now());
        response.setTotalVolume(scopedAgencyId == null
                ? transferRepository.sumTotalVolume()
                : transferRepository.sumTotalVolumeByAgency(scopedAgencyId));
        response.setTotalFees(scopedAgencyId == null
                ? transferRepository.sumTotalFees()
                : transferRepository.sumTotalFeesByAgency(scopedAgencyId));

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (TransferStatus status : TransferStatus.values()) {
            long count = scopedAgencyId == null
                    ? transferRepository.countByStatus(status)
                    : transferRepository.countBySourceAgencyIdAndStatus(scopedAgencyId, status);
            if (count > 0) {
                byStatus.put(status.name(), count);
            }
        }
        response.setTransfersByStatus(byStatus);
        response.setTotalTransfers(byStatus.values().stream().mapToLong(Long::longValue).sum());
        return response;
    }

    public AgencyReportResponse getAgencyReport(String userEmail, Long agencyId, String format) {
        User user = requireUser(userEmail);
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));
        verifyAgencyAccess(user, agency.getId());

        AgencyReportResponse response = new AgencyReportResponse();
        response.setAgencyId(agency.getId());
        response.setAgencyName(agency.getName());
        response.setAgencyCode(agency.getCode());
        response.setFormat(normalizeFormat(format));
        response.setGeneratedAt(LocalDateTime.now());
        response.setTotalTransfers(transferRepository.countBySourceAgencyId(agency.getId()));
        response.setTotalVolume(transferRepository.sumTotalVolumeByAgency(agency.getId()));
        response.setTotalAgencyCommissions(commissionRepository.sumAgencyCommission(agency.getId()));
        return response;
    }

    public CommissionsReportResponse getCommissionsReport(String userEmail, String format, Long agencyId) {
        User user = requireUser(userEmail);
        Long scopedAgencyId = resolveAgencyScope(user, agencyId);

        CommissionsReportResponse response = new CommissionsReportResponse();
        response.setFormat(normalizeFormat(format));
        response.setAgencyId(scopedAgencyId);
        response.setGeneratedAt(LocalDateTime.now());
        response.setTotalAgencyCommissions(commissionRepository.sumAgencyCommission(scopedAgencyId));
        response.setTotalCentralCommissions(commissionRepository.sumCentralCommission(scopedAgencyId));
        return response;
    }

    private User requireUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }

    private Long resolveAgencyScope(User user, Long requestedAgencyId) {
        if (user.getRole() == Role.ROLE_ADMIN) {
            return requestedAgencyId;
        }
        if (user.getRole() == Role.ROLE_MANAGER) {
            Manager manager = (Manager) user;
            if (manager.getAgency() == null) {
                throw new BusinessException("Manager sans agence assignée.");
            }
            if (requestedAgencyId != null && !manager.getAgency().getId().equals(requestedAgencyId)) {
                throw new ForbiddenOperationException("Accès refusé à cette agence.");
            }
            return manager.getAgency().getId();
        }
        throw new ForbiddenOperationException("Rapport non autorisé pour ce rôle.");
    }

    private void verifyAgencyAccess(User user, Long agencyId) {
        if (user.getRole() == Role.ROLE_ADMIN) {
            return;
        }
        if (user.getRole() == Role.ROLE_MANAGER) {
            Manager manager = (Manager) user;
            if (manager.getAgency() != null && manager.getAgency().getId().equals(agencyId)) {
                return;
            }
        }
        throw new ForbiddenOperationException("Accès refusé à cette agence.");
    }

    private String normalizeFormat(String format) {
        if (format == null || format.isBlank()) {
            return "JSON";
        }
        String normalized = format.trim().toUpperCase();
        if (!Arrays.asList("JSON", "PDF", "CSV").contains(normalized)) {
            throw new BusinessException("Format de rapport invalide: " + format);
        }
        return normalized;
    }
}
