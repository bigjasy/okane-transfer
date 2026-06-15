package ma.ensam.okanetransfer.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ma.ensam.okanetransfer.domain.agency.Agency;
import ma.ensam.okanetransfer.domain.finance.CashMovement;
import ma.ensam.okanetransfer.domain.finance.CashRegister;
import ma.ensam.okanetransfer.domain.referential.Currency;
import ma.ensam.okanetransfer.domain.user.Agent;
import ma.ensam.okanetransfer.domain.user.Manager;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.dto.finance.CashClosingRequest;
import ma.ensam.okanetransfer.dto.finance.CashMovementRequest;
import ma.ensam.okanetransfer.dto.finance.CashMovementResponse;
import ma.ensam.okanetransfer.dto.finance.CashRegisterOpenRequest;
import ma.ensam.okanetransfer.dto.finance.CashRegisterResponse;
import ma.ensam.okanetransfer.enums.CashMovementType;
import ma.ensam.okanetransfer.enums.CashRegisterStatus;
import ma.ensam.okanetransfer.enums.Role;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.exception.ForbiddenOperationException;
import ma.ensam.okanetransfer.repository.AgencyRepository;
import ma.ensam.okanetransfer.repository.CashMovementRepository;
import ma.ensam.okanetransfer.repository.CashRegisterRepository;
import ma.ensam.okanetransfer.repository.CurrencyRepository;
import ma.ensam.okanetransfer.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CashRegisterService {

    private final CashRegisterRepository cashRegisterRepository;
    private final CashMovementRepository cashMovementRepository;
    private final AgencyRepository agencyRepository;
    private final UserRepository userRepository;
    private final CurrencyRepository currencyRepository;

    public CashRegisterService(CashRegisterRepository cashRegisterRepository,
                               CashMovementRepository cashMovementRepository,
                               AgencyRepository agencyRepository,
                               UserRepository userRepository,
                               CurrencyRepository currencyRepository) {
        this.cashRegisterRepository = cashRegisterRepository;
        this.cashMovementRepository = cashMovementRepository;
        this.agencyRepository = agencyRepository;
        this.userRepository = userRepository;
        this.currencyRepository = currencyRepository;
    }

    public CashRegisterResponse openRegister(CashRegisterOpenRequest request) {
        Optional<CashRegister> existingOpenRegister = cashRegisterRepository
                .findByAgentIdAndStatus(request.getAgentId(), CashRegisterStatus.OPEN);
        
        if (existingOpenRegister.isPresent()) {
            throw new BusinessException("L'agent possède déjà une caisse ouverte.");
        }

        Agency agency = agencyRepository.findById(request.getAgencyId())
                .orElseThrow(() -> new BusinessException("Agence introuvable"));
        Agent agent = (Agent) userRepository.findById(request.getAgentId())
                .orElseThrow(() -> new BusinessException("Agent introuvable"));
        Currency currency = currencyRepository.findByCode(request.getCurrencyCode())
                .orElseThrow(() -> new BusinessException("Devise introuvable ou inactive"));

        CashRegister register = new CashRegister();
        register.setAgency(agency);
        register.setAgent(agent);
        register.setCurrency(currency);
        register.setOpeningBalance(request.getOpeningBalance());
        register.setCurrentBalance(request.getOpeningBalance());
        register.setStatus(CashRegisterStatus.OPEN);
        register.setOpenedAt(LocalDateTime.now());

        CashRegister savedRegister = cashRegisterRepository.save(register);

        return mapToResponse(savedRegister);
    }

    public CashRegisterResponse closeRegister(Long cashRegisterId, CashClosingRequest request, String userEmail) {
        CashRegister register = cashRegisterRepository.findById(cashRegisterId)
                .orElseThrow(() -> new BusinessException("Caisse introuvable"));

        if (register.getStatus() != CashRegisterStatus.OPEN) {
            throw new BusinessException("Cette caisse n'est pas ouverte.");
        }

        BigDecimal expectedBalance = register.getCurrentBalance();
        BigDecimal countedAmount = request.getCountedAmount();
        
        if (expectedBalance.compareTo(countedAmount) != 0) {
            BigDecimal difference = countedAmount.subtract(expectedBalance);
            
            User closedBy = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable"));

            CashMovement differenceMovement = new CashMovement();
            differenceMovement.setCashRegister(register);
            differenceMovement.setType(CashMovementType.CLOSING_DIFFERENCE);
            differenceMovement.setAmount(difference);
            differenceMovement.setCurrency(register.getCurrency());
            differenceMovement.setReason("Écart de clôture. Commentaire : " + request.getComment());
            differenceMovement.setCreatedBy(closedBy);
            
            cashMovementRepository.save(differenceMovement);
            register.setCurrentBalance(countedAmount);
        }

        register.setStatus(CashRegisterStatus.CLOSED);
        register.setClosedAt(LocalDateTime.now());

        CashRegister savedRegister = cashRegisterRepository.save(register);

        return mapToResponse(savedRegister);
    }

    public CashRegisterResponse getCurrentOpenRegister(String agentEmail, String currencyCode) {
        User agent = userRepository.findByEmailIgnoreCase(agentEmail)
                .orElseThrow(() -> new BusinessException("Agent introuvable avec cet email."));

        CashRegister register = cashRegisterRepository.findByAgentIdAndStatus(agent.getId(), CashRegisterStatus.OPEN)
                .orElseThrow(() -> new BusinessException("Aucune caisse n'est actuellement ouverte pour cet agent."));

        if (currencyCode != null && !register.getCurrency().getCode().equalsIgnoreCase(currencyCode)) {
            throw new BusinessException("La caisse ouverte ne correspond pas à la devise demandée.");
        }

        return mapToResponse(register);
    }

    public CashMovementResponse addManualMovement(Long id, CashMovementRequest request, String agentEmail) {
        CashRegister register = cashRegisterRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Caisse introuvable."));

        if (register.getStatus() != CashRegisterStatus.OPEN) {
            throw new BusinessException("Impossible d'ajouter un mouvement sur une caisse fermée.");
        }

        User user = userRepository.findByEmailIgnoreCase(agentEmail)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable."));

        Currency currency = currencyRepository.findByCode(request.getCurrencyCode())
                .orElseThrow(() -> new BusinessException("Devise introuvable."));

        CashMovement movement = new CashMovement();
        movement.setCashRegister(register);
        movement.setType(request.getType());
        movement.setAmount(request.getAmount());
        movement.setCurrency(currency);
        movement.setReason(request.getReason());
        movement.setCreatedBy(user);

        if (request.getType() == CashMovementType.CASH_OUT) {
            if (register.getCurrentBalance().compareTo(request.getAmount()) < 0) {
                throw new BusinessException("Solde insuffisant dans la caisse pour effectuer ce retrait.");
            }
            register.setCurrentBalance(register.getCurrentBalance().subtract(request.getAmount()));
        } else {
            register.setCurrentBalance(register.getCurrentBalance().add(request.getAmount()));
        }

        CashMovement saved = cashMovementRepository.save(movement);
        cashRegisterRepository.save(register);
        return mapMovementToResponse(saved);
    }

    public List<CashMovementResponse> getMovements(Long id, String agentEmail) {
        if (!cashRegisterRepository.existsById(id)) {
            throw new BusinessException("Caisse introuvable.");
        }
        return cashMovementRepository.findByCashRegisterId(id).stream()
                .map(this::mapMovementToResponse)
                .toList();
    }

    public List<CashRegisterResponse> listAgencyRegisters(Long agencyId, String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable."));
        verifyAgencyAccess(user, agencyId);

        return cashRegisterRepository.findByAgencyIdOrderByOpenedAtDesc(agencyId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void verifyAgencyAccess(User user, Long agencyId) {
        if (user.getRole() == Role.ROLE_ADMIN) {
            return;
        }
        if (user instanceof Manager manager
                && manager.getAgency() != null
                && manager.getAgency().getId().equals(agencyId)) {
            return;
        }
        throw new ForbiddenOperationException("Accès refusé aux caisses de cette agence.");
    }

    private CashRegisterResponse mapToResponse(CashRegister register) {
        CashRegisterResponse response = new CashRegisterResponse();
        response.setId(register.getId());
        response.setAgencyCode(register.getAgency().getCode());
        response.setAgentName(register.getAgent().getFirstName() + " " + register.getAgent().getLastName());
        response.setCurrencyCode(register.getCurrency().getCode());
        response.setOpeningBalance(register.getOpeningBalance());
        response.setCurrentBalance(register.getCurrentBalance());
        response.setStatus(register.getStatus());
        return response;
    }

    private CashMovementResponse mapMovementToResponse(CashMovement movement) {
        CashMovementResponse response = new CashMovementResponse();
        response.setId(movement.getId());
        response.setCashRegisterId(movement.getCashRegister().getId());
        response.setType(movement.getType());
        response.setAmount(movement.getAmount());
        response.setCurrencyCode(movement.getCurrency().getCode());
        response.setTransferReference(
                movement.getTransfer() != null ? movement.getTransfer().getReference() : null
        );
        response.setReason(movement.getReason());
        response.setCreatedByName(formatUserName(movement.getCreatedBy()));
        response.setCreatedAt(movement.getCreatedAt());
        return response;
    }

    private String formatUserName(User user) {
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        String fullName = (firstName + " " + lastName).trim();
        return !fullName.isBlank() ? fullName : user.getEmail();
    }
}
