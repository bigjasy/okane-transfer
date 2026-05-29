package ma.ensam.okanetransfer.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ma.ensam.okanetransfer.domain.agency.Agency;
import ma.ensam.okanetransfer.domain.agency.Corridor;
import ma.ensam.okanetransfer.domain.finance.CashMovement;
import ma.ensam.okanetransfer.domain.finance.CashRegister;
import ma.ensam.okanetransfer.domain.finance.Commission;
import ma.ensam.okanetransfer.domain.referential.Country;
import ma.ensam.okanetransfer.domain.referential.Currency;
import ma.ensam.okanetransfer.domain.transfer.Beneficiary;
import ma.ensam.okanetransfer.domain.transfer.Transfer;
import ma.ensam.okanetransfer.domain.user.Agent;
import ma.ensam.okanetransfer.domain.user.Client;
import ma.ensam.okanetransfer.dto.agency.FeeSimulationRequest;
import ma.ensam.okanetransfer.dto.agency.FeeSimulationResponse;
import ma.ensam.okanetransfer.dto.transfer.TransferCreateRequest;
import ma.ensam.okanetransfer.dto.transfer.TransferResponse;
import ma.ensam.okanetransfer.enums.AgencyStatus;
import ma.ensam.okanetransfer.enums.CashMovementType;
import ma.ensam.okanetransfer.enums.CashRegisterStatus;
import ma.ensam.okanetransfer.enums.TransferStatus;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.repository.*;
import ma.ensam.okanetransfer.util.CodeGenerator;

import java.time.LocalDateTime;

@Service
@Transactional
public class TransferService {

    private final TransferRepository transferRepository;
    private final AgencyRepository agencyRepository;
    private final CorridorRepository corridorRepository;
    private final CurrencyRepository currencyRepository;
    private final ClientRepository clientRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final CashRegisterRepository cashRegisterRepository;
    private final CashMovementRepository cashMovementRepository;
    private final CommissionRepository commissionRepository;
    private final FeeCalculationService feeCalculationService;

    public TransferService(TransferRepository transferRepository, AgencyRepository agencyRepository,
                           CorridorRepository corridorRepository, CurrencyRepository currencyRepository,
                           ClientRepository clientRepository, BeneficiaryRepository beneficiaryRepository,
                           CashRegisterRepository cashRegisterRepository, CashMovementRepository cashMovementRepository,
                           CommissionRepository commissionRepository, FeeCalculationService feeCalculationService) {
        this.transferRepository = transferRepository;
        this.agencyRepository = agencyRepository;
        this.corridorRepository = corridorRepository;
        this.currencyRepository = currencyRepository;
        this.clientRepository = clientRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.cashRegisterRepository = cashRegisterRepository;
        this.cashMovementRepository = cashMovementRepository;
        this.commissionRepository = commissionRepository;
        this.feeCalculationService = feeCalculationService;
    }

    // 1. Création du transfert (Statut: PENDING_PAYMENT)
    public TransferResponse createTransfer(TransferCreateRequest request, Long agentId) {
        // Vérification 1 : L'agence source est-elle active ?
        Agency sourceAgency = agencyRepository.findById(request.getSourceAgencyId())
                .orElseThrow(() -> new BusinessException("Agence source introuvable"));
        if (sourceAgency.getStatus() != AgencyStatus.ACTIVE) {
            throw new BusinessException("L'agence source n'est pas active.");
        }

        // Vérification 2 : Le corridor est-il actif ?
        Corridor corridor = corridorRepository.findById(request.getCorridorId())
                .orElseThrow(() -> new BusinessException("Corridor introuvable"));
        if (!corridor.isActive()) {
            throw new BusinessException("Ce corridor de transfert est actuellement inactif.");
        }

        // On simule les frais exacts via le service déjà codé
        FeeSimulationRequest simReq = new FeeSimulationRequest();
        simReq.setCorridorId(corridor.getId());
        simReq.setSourceCurrency(request.getSourceCurrency());
        simReq.setTargetCurrency(request.getTargetCurrency());
        simReq.setAmount(request.getAmount());
        FeeSimulationResponse simulation = feeCalculationService.simulateFees(simReq);

        // Récupération des acteurs
        Client sender = clientRepository.findById(request.getSenderClientId())
                .orElseThrow(() -> new BusinessException("Expéditeur introuvable"));
        
        // Note : On suppose ici que le bénéficiaire existe déjà. S'il est nouveau, il faudra le créer.
        Beneficiary beneficiary = beneficiaryRepository.findById(request.getBeneficiary().getId()) // ID à ajouter dans le DTO si besoin
                .orElseThrow(() -> new BusinessException("Bénéficiaire introuvable"));

        Agency destAgency = agencyRepository.findById(request.getDestinationAgencyId())
                .orElseThrow(() -> new BusinessException("Agence destination introuvable"));
        Currency sourceCurrency = currencyRepository.findByCode(request.getSourceCurrency()).orElseThrow();
        Currency targetCurrency = currencyRepository.findByCode(request.getTargetCurrency()).orElseThrow();

        // Construction du transfert
        Transfer transfer = new Transfer();
        transfer.setReference(CodeGenerator.generateReference());
        transfer.setWithdrawalCodeHash("PENDING"); // Sera écrasé à la confirmation
        transfer.setSender(sender);
        transfer.setBeneficiary(beneficiary);
        transfer.setSourceAgency(sourceAgency);
        transfer.setDestinationAgency(destAgency);
        transfer.setSourceCountry(corridor.getSourceCountry());
        transfer.setDestinationCountry(corridor.getDestinationCountry());
        transfer.setSourceCurrency(sourceCurrency);
        transfer.setTargetCurrency(targetCurrency);
        
        // Mappage d'un Agent factice pour l'exemple (à récupérer depuis SecurityContext dans le Controller)
        Agent agent = new Agent(); 
        agent.setId(agentId);
        transfer.setCreatedByAgent(agent);

        // Données financières
        transfer.setSentAmount(simulation.getAmount());
        transfer.setFeeAmount(simulation.getFeeAmount());
        transfer.setExchangeRateApplied(simulation.getExchangeRate());
        transfer.setReceivedAmount(simulation.getReceivedAmount());
        transfer.setChannel(request.getChannel());
        transfer.setStatus(TransferStatus.PENDING_PAYMENT); // Règle 15.1
        transfer.setExpiresAt(LocalDateTime.now().plusDays(30)); // Expiration dans 30 jours

        Transfer savedTransfer = transferRepository.save(transfer);

        // Sauvegarde des commissions
        Commission commission = new Commission();
        commission.setTransfer(savedTransfer);
        commission.setAgency(sourceAgency);
        commission.setAgencyPart(simulation.getAgencyCommission());
        commission.setCentralPart(simulation.getCentralCommission());
        commission.setCurrency(sourceCurrency);
        commissionRepository.save(commission);

        return mapToResponse(savedTransfer);
    }

    // 2. Confirmation du paiement à l'envoi (Statut: AVAILABLE)
    public String confirmPaymentAtSending(String reference, Long agentId) {
        Transfer transfer = transferRepository.findByReference(reference)
                .orElseThrow(() -> new BusinessException("Transfert introuvable"));

        if (transfer.getStatus() != TransferStatus.PENDING_PAYMENT) {
            throw new BusinessException("Ce transfert n'est pas en attente de paiement.");
        }

        // Vérification de la caisse de l'agent
        CashRegister register = cashRegisterRepository.findByAgentIdAndStatus(agentId, CashRegisterStatus.OPEN)
                .orElseThrow(() -> new BusinessException("L'agent doit avoir une caisse ouverte pour encaisser."));

        // Règle métier : Génération du code de retrait UNIQUEMENT après confirmation
        String plainWithdrawalCode = CodeGenerator.generateWithdrawalCode();
        transfer.setWithdrawalCodeHash(CodeGenerator.hashWithdrawalCode(plainWithdrawalCode));
        transfer.setStatus(TransferStatus.AVAILABLE);

        // Règle 15.3 : Création confirmée = Mouvement CASH_IN
        CashMovement movement = new CashMovement();
        movement.setCashRegister(register);
        movement.setType(CashMovementType.CASH_IN);
        // L'agent encaisse le montant + les frais
        movement.setAmount(transfer.getSentAmount().add(transfer.getFeeAmount()));
        movement.setCurrency(transfer.getSourceCurrency());
        movement.setTransfer(transfer);
        movement.setReason("Encaissement pour création du transfert " + reference);
        movement.setCreatedBy(transfer.getCreatedByAgent()); // L'agent

        cashMovementRepository.save(movement);
        
        // Mise à jour du solde de la caisse
        register.setCurrentBalance(register.getCurrentBalance().add(movement.getAmount()));
        cashRegisterRepository.save(register);

        transferRepository.save(transfer);

        // On retourne le code en clair UNE SEULE FOIS pour l'afficher à l'agent
        return plainWithdrawalCode;
    }

    // Mapper utilitaire
    private TransferResponse mapToResponse(Transfer transfer) {
        TransferResponse res = new TransferResponse();
        res.setId(transfer.getId());
        res.setReference(transfer.getReference());
        res.setStatus(transfer.getStatus());
        res.setSenderName(transfer.getSender().getFirstName() + " " + transfer.getSender().getLastName());
        res.setBeneficiaryName(transfer.getBeneficiary().getFirstName() + " " + transfer.getBeneficiary().getLastName());
        res.setSentAmount(transfer.getSentAmount());
        res.setFeeAmount(transfer.getFeeAmount());
        res.setReceivedAmount(transfer.getReceivedAmount());
        res.setSourceCurrency(transfer.getSourceCurrency().getCode());
        res.setTargetCurrency(transfer.getTargetCurrency().getCode());
        res.setExchangeRateApplied(transfer.getExchangeRateApplied());
        res.setCreatedAt(transfer.getCreatedAt());
        res.setExpiresAt(transfer.getExpiresAt());
        return res;
    }
}