package ma.ensam.okanetransfer.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ma.ensam.okanetransfer.domain.agency.Agency;
import ma.ensam.okanetransfer.domain.finance.CashMovement;
import ma.ensam.okanetransfer.domain.finance.CashRegister;
import ma.ensam.okanetransfer.domain.referential.Currency;
import ma.ensam.okanetransfer.domain.user.Agent;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.dto.finance.CashClosingRequest;
import ma.ensam.okanetransfer.dto.finance.CashRegisterOpenRequest;
import ma.ensam.okanetransfer.dto.finance.CashRegisterResponse;
import ma.ensam.okanetransfer.enums.CashMovementType;
import ma.ensam.okanetransfer.enums.CashRegisterStatus;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.repository.AgencyRepository;
import ma.ensam.okanetransfer.repository.CashMovementRepository;
import ma.ensam.okanetransfer.repository.CashRegisterRepository;
import ma.ensam.okanetransfer.repository.CurrencyRepository;
import ma.ensam.okanetransfer.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    // 1. Ouverture de la caisse
    public CashRegisterResponse openRegister(CashRegisterOpenRequest request) {
        // Règle métier : Un agent ne peut avoir qu'une seule caisse ouverte à la fois
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

    // 2. Clôture de la caisse et gestion des écarts
    public CashRegisterResponse closeRegister(Long cashRegisterId, CashClosingRequest request, Long closedById) {
        CashRegister register = cashRegisterRepository.findById(cashRegisterId)
                .orElseThrow(() -> new BusinessException("Caisse introuvable"));

        if (register.getStatus() != CashRegisterStatus.OPEN) {
            throw new BusinessException("Cette caisse n'est pas ouverte.");
        }

        BigDecimal expectedBalance = register.getCurrentBalance();
        BigDecimal countedAmount = request.getCountedAmount();
        
        // Calcul de l'écart : Si le montant compté est différent du solde théorique
        if (expectedBalance.compareTo(countedAmount) != 0) {
            BigDecimal difference = countedAmount.subtract(expectedBalance);
            
            User closedBy = userRepository.findById(closedById)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable"));

            // Génération d'un mouvement d'écart (CLOSING_DIFFERENCE)
            CashMovement differenceMovement = new CashMovement();
            differenceMovement.setCashRegister(register);
            differenceMovement.setType(CashMovementType.CLOSING_DIFFERENCE);
            differenceMovement.setAmount(difference);
            differenceMovement.setCurrency(register.getCurrency());
            differenceMovement.setReason("Écart de clôture. Commentaire : " + request.getComment());
            differenceMovement.setCreatedBy(closedBy);
            
            cashMovementRepository.save(differenceMovement);
            
            // Mise à jour du solde pour correspondre à la réalité physique
            register.setCurrentBalance(countedAmount);
        }

        register.setStatus(CashRegisterStatus.CLOSED);
        register.setClosedAt(LocalDateTime.now());

        CashRegister savedRegister = cashRegisterRepository.save(register);

        return mapToResponse(savedRegister);
    }

    // Mapper utilitaire pour convertir l'entité en DTO
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
}