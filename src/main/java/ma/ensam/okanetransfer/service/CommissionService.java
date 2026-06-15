package ma.ensam.okanetransfer.service;

import java.util.List;
import ma.ensam.okanetransfer.domain.finance.Commission;
import ma.ensam.okanetransfer.domain.user.Manager;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.dto.common.PageResponse;
import ma.ensam.okanetransfer.dto.finance.CommissionResponse;
import ma.ensam.okanetransfer.enums.Role;
import ma.ensam.okanetransfer.exception.ForbiddenOperationException;
import ma.ensam.okanetransfer.exception.ResourceNotFoundException;
import ma.ensam.okanetransfer.repository.CommissionRepository;
import ma.ensam.okanetransfer.repository.TransferRepository;
import ma.ensam.okanetransfer.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CommissionService {

    private final CommissionRepository commissionRepository;
    private final UserRepository userRepository;
    private final TransferRepository transferRepository;

    public CommissionService(
            CommissionRepository commissionRepository,
            UserRepository userRepository,
            TransferRepository transferRepository
    ) {
        this.commissionRepository = commissionRepository;
        this.userRepository = userRepository;
        this.transferRepository = transferRepository;
    }

    public PageResponse<CommissionResponse> listCommissions(String userEmail, Long agencyId, Pageable pageable) {
        User user = requireUser(userEmail);
        Long scopedAgencyId = resolveAgencyScope(user, agencyId);

        Page<Commission> page = scopedAgencyId == null
                ? commissionRepository.findAllByOrderByCreatedAtDesc(pageable)
                : commissionRepository.findByAgencyIdOrderByCreatedAtDesc(scopedAgencyId, pageable);

        return PageResponse.from(page.map(this::mapToResponse));
    }

    public List<CommissionResponse> getCommissionsByTransferReference(String userEmail, String reference) {
        User user = requireUser(userEmail);
        var transfer = transferRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", reference));

        if (user.getRole() == Role.ROLE_MANAGER) {
            Manager manager = (Manager) user;
            if (manager.getAgency() == null
                    || !manager.getAgency().getId().equals(transfer.getSourceAgency().getId())) {
                throw new ForbiddenOperationException("Accès refusé aux commissions de ce transfert.");
            }
        } else if (user.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenOperationException("Accès refusé aux commissions.");
        }

        return commissionRepository.findByTransferId(transfer.getId()).stream()
                .map(this::mapToResponse)
                .toList();
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
                throw new ForbiddenOperationException("Manager sans agence assignée.");
            }
            if (requestedAgencyId != null && !manager.getAgency().getId().equals(requestedAgencyId)) {
                throw new ForbiddenOperationException("Accès refusé à cette agence.");
            }
            return manager.getAgency().getId();
        }
        throw new ForbiddenOperationException("Liste des commissions non autorisée pour ce rôle.");
    }

    private CommissionResponse mapToResponse(Commission commission) {
        CommissionResponse response = new CommissionResponse();
        response.setId(commission.getId());
        response.setTransferReference(commission.getTransfer().getReference());
        response.setAgencyPart(commission.getAgencyPart());
        response.setCentralPart(commission.getCentralPart());
        response.setCurrency(commission.getCurrency().getCode());
        response.setAgencyName(commission.getAgency().getName());
        response.setCreatedAt(commission.getCreatedAt());
        return response;
    }
}
