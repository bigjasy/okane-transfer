package ma.ensam.okanetransfer.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import ma.ensam.okanetransfer.domain.transfer.MobileMoneyTransfer;
import ma.ensam.okanetransfer.domain.transfer.Transfer;
import ma.ensam.okanetransfer.domain.user.Agent;
import ma.ensam.okanetransfer.domain.user.Manager;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.dto.common.PageResponse;
import ma.ensam.okanetransfer.dto.compliance.AmlCheckTransferResponse;
import ma.ensam.okanetransfer.dto.transfer.MobileMoneyCallbackRequest;
import ma.ensam.okanetransfer.dto.transfer.MobileMoneyReconciliationRequest;
import ma.ensam.okanetransfer.dto.transfer.MobileMoneyReconciliationResponse;
import ma.ensam.okanetransfer.dto.transfer.MobileMoneyRequest;
import ma.ensam.okanetransfer.dto.transfer.MobileMoneyResponse;
import ma.ensam.okanetransfer.enums.AuditAction;
import ma.ensam.okanetransfer.enums.MobileMoneyOperator;
import ma.ensam.okanetransfer.enums.MobileMoneyStatus;
import ma.ensam.okanetransfer.enums.ReconciliationStatus;
import ma.ensam.okanetransfer.enums.Role;
import ma.ensam.okanetransfer.enums.TransferStatus;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.exception.ForbiddenOperationException;
import ma.ensam.okanetransfer.exception.ResourceNotFoundException;
import ma.ensam.okanetransfer.repository.MobileMoneyTransferRepository;
import ma.ensam.okanetransfer.repository.TransferRepository;
import ma.ensam.okanetransfer.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MobileMoneyService {

    private final MobileMoneyTransferRepository mobileMoneyTransferRepository;
    private final TransferRepository transferRepository;
    private final UserRepository userRepository;
    private final AmlService amlService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public MobileMoneyService(
            MobileMoneyTransferRepository mobileMoneyTransferRepository,
            TransferRepository transferRepository,
            UserRepository userRepository,
            AmlService amlService,
            AuditService auditService,
            NotificationService notificationService
    ) {
        this.mobileMoneyTransferRepository = mobileMoneyTransferRepository;
        this.transferRepository = transferRepository;
        this.userRepository = userRepository;
        this.amlService = amlService;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    public MobileMoneyResponse createTransfer(
            MobileMoneyRequest request,
            String agentEmail,
            String ipAddress,
            String userAgent
    ) {
        Agent agent = (Agent) userRepository.findByEmailIgnoreCase(agentEmail)
                .orElseThrow(() -> new BusinessException("Agent introuvable."));

        Transfer transfer = transferRepository.findByReference(request.getTransferReference().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", request.getTransferReference()));

        validateTransferForMobileMoney(transfer);
        ensureNoExistingMobileMoney(transfer);

        AmlCheckTransferResponse amlResult = amlService.checkTransfer(transfer);
        if (amlResult.isBlocked()) {
            throw new BusinessException("Transfert bloqué par la conformité AML.");
        }

        MobileMoneyTransfer mobileMoneyTransfer = new MobileMoneyTransfer();
        mobileMoneyTransfer.setTransfer(transfer);
        mobileMoneyTransfer.setOperator(request.getOperator());
        mobileMoneyTransfer.setWalletPhoneNumber(normalizePhone(request.getWalletPhoneNumber()));
        mobileMoneyTransfer.setStatus(MobileMoneyStatus.SENT_TO_OPERATOR);
        mobileMoneyTransfer.setOperatorTransactionReference(generateOperatorReference(request.getOperator()));

        MobileMoneyTransfer saved = mobileMoneyTransferRepository.save(mobileMoneyTransfer);

        notificationService.notifyMobileMoneyWallet(
                saved.getWalletPhoneNumber(),
                transfer.getReference(),
                saved.getOperator().name(),
                saved.getOperatorTransactionReference()
        );

        auditService.record(
                AuditAction.MOBILE_MONEY_SEND,
                agent,
                "MobileMoneyTransfer",
                String.valueOf(saved.getId()),
                ipAddress,
                userAgent,
                "{\"transferReference\":\"" + transfer.getReference() + "\",\"operator\":\"" + saved.getOperator() + "\"}"
        );

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public MobileMoneyResponse getById(Long id, String userEmail) {
        MobileMoneyTransfer transfer = findMobileMoneyTransfer(id);
        verifyReadAccess(transfer, userEmail);
        return toResponse(transfer);
    }

    @Transactional(readOnly = true)
    public PageResponse<MobileMoneyResponse> listTransfers(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        Page<MobileMoneyTransfer> page = switch (user.getRole()) {
            case ROLE_ADMIN -> mobileMoneyTransferRepository.findAllByOrderByCreatedAtDesc(pageable);
            case ROLE_MANAGER -> {
                Manager manager = (Manager) user;
                if (manager.getAgency() == null) {
                    throw new BusinessException("Manager sans agence assignée.");
                }
                yield mobileMoneyTransferRepository.findByAgencyId(manager.getAgency().getId(), pageable);
            }
            case ROLE_AGENT -> {
                Agent agent = (Agent) user;
                if (agent.getAgency() == null) {
                    throw new BusinessException("Agent sans agence assignée.");
                }
                yield mobileMoneyTransferRepository.findByAgentId(agent.getId(), pageable);
            }
            default -> throw new ForbiddenOperationException("Accès Mobile Money non autorisé pour ce rôle.");
        };

        return PageResponse.from(page.map(this::toResponse));
    }

    public MobileMoneyResponse simulateCallback(
            Long id,
            MobileMoneyCallbackRequest request,
            String adminEmail,
            String ipAddress,
            String userAgent
    ) {
        User admin = userRepository.findByEmailIgnoreCase(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", adminEmail));

        MobileMoneyTransfer mobileMoneyTransfer = findMobileMoneyTransfer(id);
        if (mobileMoneyTransfer.getStatus() != MobileMoneyStatus.SENT_TO_OPERATOR
                && mobileMoneyTransfer.getStatus() != MobileMoneyStatus.PENDING) {
            throw new BusinessException("Seuls les transferts en attente de callback peuvent être confirmés.");
        }

        MobileMoneyStatus targetStatus = request != null && request.getStatus() != null
                ? request.getStatus()
                : MobileMoneyStatus.CONFIRMED;
        if (targetStatus != MobileMoneyStatus.CONFIRMED && targetStatus != MobileMoneyStatus.FAILED) {
            throw new BusinessException("Statut de callback invalide.");
        }

        mobileMoneyTransfer.setStatus(targetStatus);
        if (request != null && request.getOperatorTransactionReference() != null
                && !request.getOperatorTransactionReference().isBlank()) {
            mobileMoneyTransfer.setOperatorTransactionReference(request.getOperatorTransactionReference().trim());
        } else if (mobileMoneyTransfer.getOperatorTransactionReference() == null
                || mobileMoneyTransfer.getOperatorTransactionReference().isBlank()) {
            mobileMoneyTransfer.setOperatorTransactionReference(
                    generateOperatorReference(mobileMoneyTransfer.getOperator())
            );
        }

        if (targetStatus == MobileMoneyStatus.CONFIRMED) {
            Transfer transfer = mobileMoneyTransfer.getTransfer();
            transfer.setStatus(TransferStatus.PAID);
            transfer.setPaidAt(LocalDateTime.now());
            transferRepository.save(transfer);
        }

        MobileMoneyTransfer saved = mobileMoneyTransferRepository.save(mobileMoneyTransfer);

        auditService.record(
                AuditAction.MOBILE_MONEY_CALLBACK,
                admin,
                "MobileMoneyTransfer",
                String.valueOf(saved.getId()),
                ipAddress,
                userAgent,
                "{\"status\":\"" + saved.getStatus() + "\"}"
        );

        return toResponse(saved);
    }

    public MobileMoneyReconciliationResponse reconcile(
            MobileMoneyReconciliationRequest request,
            String userEmail,
            String ipAddress,
            String userAgent
    ) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        if (user.getRole() != Role.ROLE_ADMIN && user.getRole() != Role.ROLE_MANAGER) {
            throw new ForbiddenOperationException("Réconciliation réservée aux administrateurs et managers.");
        }

        LocalDate reconciliationDate = LocalDate.parse(request.getDate());
        LocalDateTime start = reconciliationDate.atStartOfDay();
        LocalDateTime end = reconciliationDate.plusDays(1).atStartOfDay();

        List<MobileMoneyTransfer> transfers = mobileMoneyTransferRepository
                .findByOperatorAndCreatedAtBetween(request.getOperator(), start, end);

        int reconciled = 0;
        int mismatches = 0;

        for (MobileMoneyTransfer transfer : transfers) {
            if (transfer.getStatus() == MobileMoneyStatus.CONFIRMED) {
                if (transfer.getReconciliationStatus() == ReconciliationStatus.NOT_RECONCILED) {
                    transfer.setReconciliationStatus(ReconciliationStatus.RECONCILED);
                    transfer.setReconciledAt(LocalDateTime.now());
                    reconciled++;
                }
            } else if (transfer.getStatus() == MobileMoneyStatus.SENT_TO_OPERATOR
                    || transfer.getStatus() == MobileMoneyStatus.FAILED) {
                transfer.setReconciliationStatus(ReconciliationStatus.MISMATCH);
                mismatches++;
            }
        }

        mobileMoneyTransferRepository.saveAll(transfers);

        auditService.record(
                AuditAction.MOBILE_MONEY_RECONCILE,
                user,
                "MobileMoneyTransfer",
                request.getOperator().name(),
                ipAddress,
                userAgent,
                "{\"date\":\"" + request.getDate() + "\",\"reconciled\":" + reconciled + ",\"mismatches\":" + mismatches + "}"
        );

        return new MobileMoneyReconciliationResponse(reconciled, mismatches);
    }

    private void validateTransferForMobileMoney(Transfer transfer) {
        if (transfer.getStatus() != TransferStatus.AVAILABLE) {
            throw new BusinessException("Le transfert doit être en statut AVAILABLE pour un envoi Mobile Money.");
        }
        if (transfer.getExpiresAt() != null && transfer.getExpiresAt().isBefore(LocalDateTime.now())) {
            transfer.setStatus(TransferStatus.EXPIRED);
            transferRepository.save(transfer);
            throw new BusinessException("Ce transfert a expiré.");
        }
    }

    private void ensureNoExistingMobileMoney(Transfer transfer) {
        mobileMoneyTransferRepository.findByTransferId(transfer.getId()).ifPresent(existing -> {
            throw new BusinessException("Un envoi Mobile Money existe déjà pour ce transfert.");
        });
    }

    private MobileMoneyTransfer findMobileMoneyTransfer(Long id) {
        return mobileMoneyTransferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MobileMoneyTransfer", id));
    }

    private void verifyReadAccess(MobileMoneyTransfer mobileMoneyTransfer, String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        if (user.getRole() == Role.ROLE_ADMIN) {
            return;
        }

        Transfer transfer = mobileMoneyTransfer.getTransfer();
        if (user.getRole() == Role.ROLE_AGENT) {
            Agent agent = (Agent) user;
            if (transfer.getCreatedByAgent().getId().equals(agent.getId())) {
                return;
            }
            if (agent.getAgency() != null
                    && (transfer.getSourceAgency().getId().equals(agent.getAgency().getId())
                    || transfer.getDestinationAgency().getId().equals(agent.getAgency().getId()))) {
                return;
            }
        }

        if (user.getRole() == Role.ROLE_MANAGER) {
            Manager manager = (Manager) user;
            if (manager.getAgency() != null
                    && (transfer.getSourceAgency().getId().equals(manager.getAgency().getId())
                    || transfer.getDestinationAgency().getId().equals(manager.getAgency().getId()))) {
                return;
            }
        }

        throw new ForbiddenOperationException("Accès refusé à ce transfert Mobile Money.");
    }

    private MobileMoneyResponse toResponse(MobileMoneyTransfer entity) {
        MobileMoneyResponse response = new MobileMoneyResponse();
        response.setId(entity.getId());
        response.setTransferReference(entity.getTransfer().getReference());
        response.setOperator(entity.getOperator());
        response.setStatus(entity.getStatus());
        response.setReconciliationStatus(entity.getReconciliationStatus());
        response.setOperatorTransactionReference(entity.getOperatorTransactionReference());
        response.setWalletPhoneNumber(entity.getWalletPhoneNumber());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }

    private String normalizePhone(String phone) {
        String trimmed = phone.trim().replace(" ", "");
        if (trimmed.startsWith("+")) {
            return trimmed;
        }
        if (trimmed.startsWith("00")) {
            return "+" + trimmed.substring(2);
        }
        if (trimmed.startsWith("0")) {
            return "+212" + trimmed.substring(1);
        }
        return "+" + trimmed;
    }

    private String generateOperatorReference(MobileMoneyOperator operator) {
        String prefix = switch (operator) {
            case ORANGE_MONEY -> "OM";
            case WAVE -> "WV";
            case MPESA -> "MP";
        };
        return prefix + "-" + System.currentTimeMillis() + "-" + ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
