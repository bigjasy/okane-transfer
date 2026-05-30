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
import ma.ensam.okanetransfer.dto.transfer.BeneficiaryRequest;
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
    private final UserRepository userRepository;
    private final CountryRepository countryRepository;
    private final AmlService amlService;

    public TransferService(TransferRepository transferRepository, AgencyRepository agencyRepository,
                           CorridorRepository corridorRepository, CurrencyRepository currencyRepository,
                           ClientRepository clientRepository, BeneficiaryRepository beneficiaryRepository,
                           CashRegisterRepository cashRegisterRepository, CashMovementRepository cashMovementRepository,
                           CommissionRepository commissionRepository, FeeCalculationService feeCalculationService,
                           UserRepository userRepository, CountryRepository countryRepository, AmlService amlService) {
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
        this.userRepository = userRepository;
        this.countryRepository = countryRepository;
        this.amlService = amlService;
    }

    public TransferResponse createTransfer(TransferCreateRequest request, String agentEmail) {
        Agent agent = (Agent) userRepository.findByEmailIgnoreCase(agentEmail)
                .orElseThrow(() -> new BusinessException("Agent introuvable avec cet email"));

        Agency sourceAgency = agencyRepository.findById(request.getSourceAgencyId())
                .orElseThrow(() -> new BusinessException("Agence source introuvable"));
        if (sourceAgency.getStatus() != AgencyStatus.ACTIVE) {
            throw new BusinessException("L'agence source n'est pas active.");
        }

        Corridor corridor = corridorRepository.findById(request.getCorridorId())
                .orElseThrow(() -> new BusinessException("Corridor introuvable"));
        if (!corridor.isActive()) {
            throw new BusinessException("Ce corridor de transfert est actuellement inactif.");
        }

        FeeSimulationRequest simReq = new FeeSimulationRequest();
        simReq.setCorridorId(corridor.getId());
        simReq.setSourceCurrency(request.getSourceCurrency());
        simReq.setTargetCurrency(request.getTargetCurrency());
        simReq.setAmount(request.getAmount());
        FeeSimulationResponse simulation = feeCalculationService.simulateFees(simReq);

        Client sender = clientRepository.findById(request.getSenderClientId())
                .orElseThrow(() -> new BusinessException("Expéditeur introuvable"));
        Beneficiary beneficiary = resolveBeneficiary(request.getBeneficiary(), sender);

        Agency destAgency = agencyRepository.findById(request.getDestinationAgencyId())
                .orElseThrow(() -> new BusinessException("Agence destination introuvable"));
        Currency sourceCurrency = currencyRepository.findByCode(request.getSourceCurrency()).orElseThrow();
        Currency targetCurrency = currencyRepository.findByCode(request.getTargetCurrency()).orElseThrow();

        Transfer transfer = new Transfer();
        transfer.setReference(CodeGenerator.generateReference());
        transfer.setWithdrawalCodeHash("PENDING");
        transfer.setSender(sender);
        transfer.setBeneficiary(beneficiary);
        transfer.setSourceAgency(sourceAgency);
        transfer.setDestinationAgency(destAgency);
        transfer.setSourceCountry(corridor.getSourceCountry());
        transfer.setDestinationCountry(corridor.getDestinationCountry());
        transfer.setSourceCurrency(sourceCurrency);
        transfer.setTargetCurrency(targetCurrency);
        transfer.setCreatedByAgent(agent);
        transfer.setSentAmount(simulation.getAmount());
        transfer.setFeeAmount(simulation.getFeeAmount());
        transfer.setExchangeRateApplied(simulation.getExchangeRate());
        transfer.setReceivedAmount(simulation.getReceivedAmount());
        transfer.setChannel(request.getChannel());
        transfer.setStatus(TransferStatus.PENDING_PAYMENT);
        transfer.setExpiresAt(LocalDateTime.now().plusDays(30));

        Transfer savedTransfer = transferRepository.save(transfer);

        if (amlService.checkTransfer(savedTransfer).isBlocked()) {
            return mapToResponse(transferRepository.findById(savedTransfer.getId()).orElseThrow());
        }

        Commission commission = new Commission();
        commission.setTransfer(savedTransfer);
        commission.setAgency(sourceAgency);
        commission.setAgencyPart(simulation.getAgencyCommission());
        commission.setCentralPart(simulation.getCentralCommission());
        commission.setCurrency(sourceCurrency);
        commissionRepository.save(commission);

        return mapToResponse(savedTransfer);
    }

    public String confirmPaymentAtSending(String reference, String agentEmail) {
        Transfer transfer = transferRepository.findByReference(reference)
                .orElseThrow(() -> new BusinessException("Transfert introuvable"));

        if (transfer.getStatus() == TransferStatus.BLOCKED_AML) {
            throw new BusinessException("Ce transfert est bloqué pour conformité AML.");
        }
        if (transfer.getStatus() != TransferStatus.PENDING_PAYMENT) {
            throw new BusinessException("Ce transfert n'est pas en attente de paiement.");
        }

        Agent agent = (Agent) userRepository.findByEmailIgnoreCase(agentEmail)
                .orElseThrow(() -> new BusinessException("Agent introuvable"));

        CashRegister register = cashRegisterRepository.findByAgentIdAndStatus(agent.getId(), CashRegisterStatus.OPEN)
                .orElseThrow(() -> new BusinessException("L'agent doit avoir une caisse ouverte pour encaisser."));

        String plainWithdrawalCode = CodeGenerator.generateWithdrawalCode();
        transfer.setWithdrawalCodeHash(CodeGenerator.hashWithdrawalCode(plainWithdrawalCode));
        transfer.setStatus(TransferStatus.AVAILABLE);

        CashMovement movement = new CashMovement();
        movement.setCashRegister(register);
        movement.setType(CashMovementType.CASH_IN);
        movement.setAmount(transfer.getSentAmount().add(transfer.getFeeAmount()));
        movement.setCurrency(transfer.getSourceCurrency());
        movement.setTransfer(transfer);
        movement.setReason("Encaissement pour création du transfert " + reference);
        movement.setCreatedBy(agent);

        cashMovementRepository.save(movement);
        
        register.setCurrentBalance(register.getCurrentBalance().add(movement.getAmount()));
        cashRegisterRepository.save(register);
        transferRepository.save(transfer);

        return plainWithdrawalCode;
    }

    private Beneficiary resolveBeneficiary(BeneficiaryRequest request, Client sender) {
        if (request.getId() != null) {
            Beneficiary existing = beneficiaryRepository.findById(request.getId())
                    .orElseThrow(() -> new BusinessException("Bénéficiaire introuvable"));
            if (!existing.getClient().getId().equals(sender.getId())) {
                throw new BusinessException("Ce bénéficiaire n'appartient pas à l'expéditeur.");
            }
            return existing;
        }

        if (request.getFirstName() == null || request.getFirstName().isBlank()
                || request.getLastName() == null || request.getLastName().isBlank()
                || request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()
                || request.getCountryId() == null
                || request.getIdentityType() == null
                || request.getIdentityNumber() == null || request.getIdentityNumber().isBlank()) {
            throw new BusinessException("Informations du bénéficiaire incomplètes.");
        }

        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new BusinessException("Pays du bénéficiaire introuvable"));

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setClient(sender);
        beneficiary.setFirstName(request.getFirstName().trim());
        beneficiary.setLastName(request.getLastName().trim());
        beneficiary.setPhoneNumber(request.getPhoneNumber().trim());
        beneficiary.setCountry(country);
        beneficiary.setIdentityType(request.getIdentityType());
        beneficiary.setIdentityNumberEncrypted(request.getIdentityNumber().trim());
        return beneficiaryRepository.save(beneficiary);
    }

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