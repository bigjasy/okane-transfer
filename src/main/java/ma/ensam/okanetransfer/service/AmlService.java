package ma.ensam.okanetransfer.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import ma.ensam.okanetransfer.domain.compliance.AmlAlert;
import ma.ensam.okanetransfer.domain.compliance.WatchlistEntry;
import ma.ensam.okanetransfer.domain.transfer.Beneficiary;
import ma.ensam.okanetransfer.domain.transfer.Transfer;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.dto.compliance.AmlAlertResponse;
import ma.ensam.okanetransfer.dto.compliance.AmlCheckTransferResponse;
import ma.ensam.okanetransfer.dto.compliance.AmlReviewRequest;
import ma.ensam.okanetransfer.dto.compliance.ComplianceSummaryResponse;
import ma.ensam.okanetransfer.dto.common.PageResponse;
import ma.ensam.okanetransfer.enums.AmlAlertStatus;
import ma.ensam.okanetransfer.enums.AmlAlertType;
import ma.ensam.okanetransfer.enums.KycStatus;
import ma.ensam.okanetransfer.enums.AuditAction;
import ma.ensam.okanetransfer.enums.RiskLevel;
import ma.ensam.okanetransfer.enums.TransferStatus;
import ma.ensam.okanetransfer.exception.ResourceNotFoundException;
import ma.ensam.okanetransfer.repository.AmlAlertRepository;
import ma.ensam.okanetransfer.repository.KycDocumentRepository;
import ma.ensam.okanetransfer.repository.TransferRepository;
import ma.ensam.okanetransfer.repository.UserRepository;
import ma.ensam.okanetransfer.repository.WatchlistEntryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AmlService {

    private final AmlAlertRepository amlAlertRepository;
    private final WatchlistEntryRepository watchlistEntryRepository;
    private final TransferRepository transferRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final KycDocumentRepository kycDocumentRepository;
    private final BigDecimal threshold;

    public AmlService(
            AmlAlertRepository amlAlertRepository,
            WatchlistEntryRepository watchlistEntryRepository,
            TransferRepository transferRepository,
            UserRepository userRepository,
            AuditService auditService,
            NotificationService notificationService,
            KycDocumentRepository kycDocumentRepository,
            @Value("${aml.threshold:10000}") BigDecimal threshold
    ) {
        this.amlAlertRepository = amlAlertRepository;
        this.watchlistEntryRepository = watchlistEntryRepository;
        this.transferRepository = transferRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.kycDocumentRepository = kycDocumentRepository;
        this.threshold = threshold;
    }

    @Transactional(readOnly = true)
    public PageResponse<AmlAlertResponse> listAlerts(AmlAlertStatus status, RiskLevel riskLevel, Pageable pageable) {
        Page<AmlAlert> page;
        if (status != null) {
            page = amlAlertRepository.findByStatus(status, pageable);
        } else if (riskLevel != null) {
            page = amlAlertRepository.findByRiskLevel(riskLevel, pageable);
        } else {
            page = amlAlertRepository.findAll(pageable);
        }
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public AmlAlertResponse getAlert(Long id) {
        return toResponse(findAlert(id));
    }

    public AmlAlertResponse reviewAlert(
            Long id,
            AmlReviewRequest request,
            String reviewerEmail,
            String ipAddress,
            String userAgent
    ) {
        AmlAlert alert = findAlert(id);
        User reviewer = userRepository.findByEmailIgnoreCase(reviewerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", reviewerEmail));

        alert.setStatus(request.getStatus());
        alert.setReviewedBy(reviewer);
        alert.setReviewedAt(LocalDateTime.now());
        if (request.getComment() != null && !request.getComment().isBlank()) {
            alert.setDescription(alert.getDescription() + " | Review: " + request.getComment());
        }

        AmlAlert saved = amlAlertRepository.save(alert);
        auditService.record(
                AuditAction.AML_REVIEW,
                reviewer,
                "AmlAlert",
                String.valueOf(saved.getId()),
                ipAddress,
                userAgent,
                "{\"status\":\"" + request.getStatus() + "\"}"
        );
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ComplianceSummaryResponse getComplianceSummary() {
        ComplianceSummaryResponse summary = new ComplianceSummaryResponse();
        summary.setPendingKycDocuments(kycDocumentRepository.countByStatus(KycStatus.PENDING));
        summary.setOpenAmlAlerts(amlAlertRepository.countByStatus(AmlAlertStatus.OPEN));
        summary.setCriticalAmlAlerts(
                amlAlertRepository.countByRiskLevelAndStatus(RiskLevel.CRITICAL, AmlAlertStatus.OPEN)
                        + amlAlertRepository.countByRiskLevelAndStatus(RiskLevel.CRITICAL, AmlAlertStatus.UNDER_REVIEW)
        );
        summary.setActiveWatchlistEntries(watchlistEntryRepository.countByActiveTrue());
        summary.setBlockedTransfers(transferRepository.countByStatus(TransferStatus.BLOCKED_AML));
        return summary;
    }

    public AmlCheckTransferResponse checkTransfer(String transferReference) {
        Transfer transfer = transferRepository.findByReference(transferReference)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", transferReference));
        return checkTransfer(transfer);
    }

    public AmlCheckTransferResponse checkTransfer(Transfer transfer) {
        List<AmlAlert> alerts = new ArrayList<>();
        BigDecimal threshold = resolveThreshold();

        if (transfer.getSentAmount().compareTo(threshold) > 0) {
            alerts.add(createAlert(
                    transfer,
                    AmlAlertType.THRESHOLD_EXCEEDED,
                    RiskLevel.HIGH,
                    "Transfer amount exceeds AML threshold of " + threshold
            ));
        }

        Beneficiary beneficiary = transfer.getBeneficiary();
        List<WatchlistEntry> watchlistMatches = watchlistEntryRepository
                .findByLastNameIgnoreCaseAndActiveTrue(beneficiary.getLastName());
        for (WatchlistEntry entry : watchlistMatches) {
            if (namesMatch(entry.getFirstName(), beneficiary.getFirstName())
                    && countriesMatch(entry, beneficiary)) {
                alerts.add(createAlert(
                        transfer,
                        AmlAlertType.WATCHLIST_MATCH,
                        RiskLevel.CRITICAL,
                        "Beneficiary matches watchlist entry: " + entry.getFirstName() + " " + entry.getLastName()
                ));
            }
        }

        boolean blocked = alerts.stream().anyMatch(alert -> alert.getRiskLevel() == RiskLevel.CRITICAL);
        if (blocked) {
            transfer.setStatus(TransferStatus.BLOCKED_AML);
            transferRepository.save(transfer);
            notificationService.notifyAmlAlert(transfer, alerts);
        }

        List<AmlAlertResponse> responses = alerts.stream().map(this::toResponse).toList();
        return new AmlCheckTransferResponse(blocked, responses);
    }

    private AmlAlert createAlert(Transfer transfer, AmlAlertType type, RiskLevel riskLevel, String description) {
        AmlAlert alert = new AmlAlert();
        alert.setTransfer(transfer);
        alert.setUser(transfer.getSender());
        alert.setType(type);
        alert.setRiskLevel(riskLevel);
        alert.setStatus(AmlAlertStatus.OPEN);
        alert.setDescription(description);
        return amlAlertRepository.save(alert);
    }

    private boolean namesMatch(String watchlistFirstName, String beneficiaryFirstName) {
        return watchlistFirstName != null
                && beneficiaryFirstName != null
                && watchlistFirstName.equalsIgnoreCase(beneficiaryFirstName);
    }

    private boolean countriesMatch(WatchlistEntry entry, Beneficiary beneficiary) {
        if (entry.getCountry() == null || beneficiary.getCountry() == null) {
            return true;
        }
        return entry.getCountry().getId().equals(beneficiary.getCountry().getId());
    }

    private BigDecimal resolveThreshold() {
        return threshold;
    }

    private AmlAlert findAlert(Long id) {
        return amlAlertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AmlAlert", id));
    }

    private AmlAlertResponse toResponse(AmlAlert alert) {
        AmlAlertResponse response = new AmlAlertResponse();
        response.setId(alert.getId());
        if (alert.getTransfer() != null) {
            response.setTransferReference(alert.getTransfer().getReference());
        }
        response.setType(alert.getType());
        response.setRiskLevel(alert.getRiskLevel());
        response.setStatus(alert.getStatus());
        response.setDescription(alert.getDescription());
        response.setCreatedAt(alert.getCreatedAt());
        return response;
    }
}
