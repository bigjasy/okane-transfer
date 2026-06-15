package ma.ensam.okanetransfer.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ma.ensam.okanetransfer.domain.finance.CashMovement;
import ma.ensam.okanetransfer.domain.finance.CashRegister;
import ma.ensam.okanetransfer.domain.transfer.Transfer;
import ma.ensam.okanetransfer.domain.transfer.TransferPayment;
import ma.ensam.okanetransfer.domain.user.Agent;
import ma.ensam.okanetransfer.dto.transfer.PayoutConfirmRequest;
import ma.ensam.okanetransfer.dto.transfer.PayoutReceiptResponse;
import ma.ensam.okanetransfer.dto.transfer.PayoutResponse;
import ma.ensam.okanetransfer.dto.transfer.PayoutSearchRequest;
import ma.ensam.okanetransfer.dto.transfer.PayoutSearchResponse;
import ma.ensam.okanetransfer.dto.transfer.PayoutValidateRequest;
import ma.ensam.okanetransfer.dto.transfer.PayoutValidateResponse;
import ma.ensam.okanetransfer.enums.CashMovementType;
import ma.ensam.okanetransfer.enums.CashRegisterStatus;
import ma.ensam.okanetransfer.enums.TransferStatus;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.exception.ResourceNotFoundException;
import ma.ensam.okanetransfer.repository.CashMovementRepository;
import ma.ensam.okanetransfer.repository.CashRegisterRepository;
import ma.ensam.okanetransfer.repository.TransferPaymentRepository;
import ma.ensam.okanetransfer.repository.TransferRepository;
import ma.ensam.okanetransfer.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PayoutService {

    private final TransferRepository transferRepository;
    private final TransferPaymentRepository transferPaymentRepository;
    private final CashRegisterRepository cashRegisterRepository;
    private final CashMovementRepository cashMovementRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public PayoutService(TransferRepository transferRepository, 
                         TransferPaymentRepository transferPaymentRepository,
                         CashRegisterRepository cashRegisterRepository, 
                         CashMovementRepository cashMovementRepository,
                         PasswordEncoder passwordEncoder,
                         UserRepository userRepository) {
        this.transferRepository = transferRepository;
        this.transferPaymentRepository = transferPaymentRepository;
        this.cashRegisterRepository = cashRegisterRepository;
        this.cashMovementRepository = cashMovementRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public PayoutSearchResponse searchTransfer(PayoutSearchRequest request) {
        Transfer transfer = resolveSearchTransfer(request);
        markExpiredIfNeeded(transfer);
        return mapToSearchResponse(transfer);
    }

    public PayoutValidateResponse validateBeneficiary(PayoutValidateRequest request) {
        Transfer transfer = transferRepository.findByReference(request.getTransferReference())
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", request.getTransferReference()));

        if (transfer.getStatus() != TransferStatus.AVAILABLE) {
            throw new BusinessException("Le transfert n'est pas disponible pour vérification.");
        }
        if (markExpiredIfNeeded(transfer)) {
            throw new BusinessException("Ce transfert a expiré et ne peut plus être payé.");
        }

        if (!matchesWithdrawalCode(request.getWithdrawalCode(), transfer)) {
            return new PayoutValidateResponse(false, false, "Code de retrait invalide.");
        }

        if (transfer.getBeneficiary().getIdentityType() != request.getIdentityType()) {
            return new PayoutValidateResponse(false, false, "Le type de pièce d'identité ne correspond pas.");
        }

        String expectedIdentity = transfer.getBeneficiary().getIdentityNumberEncrypted();
        if (expectedIdentity == null || !expectedIdentity.equalsIgnoreCase(request.getIdentityNumber().trim())) {
            return new PayoutValidateResponse(false, false, "Le numéro de pièce d'identité est incorrect.");
        }

        return new PayoutValidateResponse(true, false, "Identité bénéficiaire validée.");
    }

    public PayoutResponse confirmPayout(PayoutConfirmRequest request, String agentEmail) {
        Agent agent = (Agent) userRepository.findByEmailIgnoreCase(agentEmail)
                .orElseThrow(() -> new BusinessException("Agent introuvable"));

        Transfer transfer = transferRepository.findByReference(request.getTransferReference())
                .orElseThrow(() -> new BusinessException("Transfert introuvable."));

        if (transfer.getStatus() != TransferStatus.AVAILABLE) {
            throw new BusinessException("Ce transfert n'est pas disponible pour le retrait. Statut actuel : " + transfer.getStatus());
        }

        if (transfer.getExpiresAt().isBefore(LocalDateTime.now())) {
            transfer.setStatus(TransferStatus.EXPIRED);
            transferRepository.save(transfer);
            throw new BusinessException("Ce transfert a expiré et ne peut plus être payé.");
        }

        if (!passwordEncoder.matches(request.getWithdrawalCode(), transfer.getWithdrawalCodeHash())) {
            throw new BusinessException("Code de retrait invalide.");
        }

        CashRegister register = cashRegisterRepository.findByAgentIdAndStatus(agent.getId(), CashRegisterStatus.OPEN)
                .orElseThrow(() -> new BusinessException("Vous devez avoir une caisse ouverte pour effectuer un paiement."));

        if (!register.getCurrency().getCode().equals(transfer.getTargetCurrency().getCode())) {
            throw new BusinessException("La devise de votre caisse ne correspond pas à la devise de réception du transfert.");
        }

        if (register.getCurrentBalance().compareTo(transfer.getReceivedAmount()) < 0) {
            throw new BusinessException("Fonds insuffisants dans la caisse pour payer ce transfert.");
        }

        transfer.setStatus(TransferStatus.PAID);
        transfer.setPaidAt(LocalDateTime.now());
        transferRepository.save(transfer);

        TransferPayment payment = new TransferPayment();
        payment.setTransfer(transfer);
        payment.setPaidByAgent(agent);
        payment.setPaidAtAgency(register.getAgency());
        payment.setBeneficiaryIdentityType(request.getIdentityType());
        payment.setBeneficiaryIdentityNumberEncrypted(request.getIdentityNumber());
        payment.setPaidAmount(transfer.getReceivedAmount());
        transferPaymentRepository.save(payment);

        CashMovement movement = new CashMovement();
        movement.setCashRegister(register);
        movement.setType(CashMovementType.CASH_OUT);
        movement.setAmount(transfer.getReceivedAmount());
        movement.setCurrency(transfer.getTargetCurrency());
        movement.setTransfer(transfer);
        movement.setReason("Paiement du transfert " + transfer.getReference());
        movement.setCreatedBy(agent);
        cashMovementRepository.save(movement);

        register.setCurrentBalance(register.getCurrentBalance().subtract(transfer.getReceivedAmount()));
        cashRegisterRepository.save(register);

        PayoutResponse response = new PayoutResponse();
        response.setTransferReference(transfer.getReference());
        response.setStatus(transfer.getStatus());
        response.setPaidAmount(payment.getPaidAmount());
        response.setCurrency(transfer.getTargetCurrency().getCode());
        response.setPaidAt(transfer.getPaidAt());
        response.setBeneficiaryName(transfer.getBeneficiary().getFirstName() + " " + transfer.getBeneficiary().getLastName());
        response.setAgentName(agent.getFirstName() + " " + agent.getLastName());
        response.setAgencyName(register.getAgency().getName());
        response.setMaskedIdentityNumber(maskIdentity(request.getIdentityNumber()));

        return response;
    }

    @Transactional(readOnly = true)
    public PayoutReceiptResponse getReceipt(String transferReference) {
        Transfer transfer = transferRepository.findByReference(transferReference)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", transferReference));

        if (transfer.getStatus() != TransferStatus.PAID) {
            throw new BusinessException("Le reçu de paiement n'est disponible que pour les transferts payés.");
        }

        TransferPayment payment = transferPaymentRepository.findByTransferId(transfer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("TransferPayment", transferReference));

        PayoutReceiptResponse response = new PayoutReceiptResponse();
        response.setTransferReference(transfer.getReference());
        response.setBeneficiaryName(transfer.getBeneficiary().getFirstName() + " " + transfer.getBeneficiary().getLastName());
        response.setPaidAmount(payment.getPaidAmount());
        response.setCurrency(transfer.getTargetCurrency().getCode());
        response.setPaidAt(payment.getPaidAt());
        response.setStatus(transfer.getStatus());
        response.setAgentName(payment.getPaidByAgent().getFirstName() + " " + payment.getPaidByAgent().getLastName());
        response.setAgencyName(payment.getPaidAtAgency().getName());
        response.setMaskedIdentityNumber(maskIdentity(payment.getBeneficiaryIdentityNumberEncrypted()));
        return response;
    }

    private Transfer resolveSearchTransfer(PayoutSearchRequest request) {
        String reference = normalize(request.getReference());
        if (reference != null) {
            return transferRepository.findByReference(reference)
                    .orElseThrow(() -> new ResourceNotFoundException("Transfer", reference));
        }

        String withdrawalCode = normalize(request.getWithdrawalCode());
        String beneficiaryPhoneNumber = normalize(request.getBeneficiaryPhoneNumber());
        if (withdrawalCode == null && beneficiaryPhoneNumber == null) {
            throw new BusinessException("Veuillez fournir une référence, un code de retrait ou un téléphone bénéficiaire.");
        }

        List<Transfer> candidates = beneficiaryPhoneNumber != null
                ? transferRepository.findByBeneficiaryPhoneNumber(beneficiaryPhoneNumber)
                : transferRepository.findByStatus(TransferStatus.AVAILABLE);

        return candidates.stream()
                .filter(transfer -> withdrawalCode == null || matchesWithdrawalCode(withdrawalCode, transfer))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", "payout search"));
    }

    private boolean markExpiredIfNeeded(Transfer transfer) {
        if (transfer.getExpiresAt() != null
                && transfer.getExpiresAt().isBefore(LocalDateTime.now())
                && transfer.getStatus() != TransferStatus.PAID
                && transfer.getStatus() != TransferStatus.CANCELLED
                && transfer.getStatus() != TransferStatus.EXPIRED) {
            transfer.setStatus(TransferStatus.EXPIRED);
            transferRepository.save(transfer);
            return true;
        }
        return transfer.getStatus() == TransferStatus.EXPIRED;
    }

    private boolean matchesWithdrawalCode(String withdrawalCode, Transfer transfer) {
        if (withdrawalCode == null || withdrawalCode.isBlank()) {
            return false;
        }
        String hash = transfer.getWithdrawalCodeHash();
        if (hash == null || hash.isBlank() || "PENDING".equalsIgnoreCase(hash)) {
            return false;
        }
        return passwordEncoder.matches(withdrawalCode.trim(), hash);
    }

    private PayoutSearchResponse mapToSearchResponse(Transfer transfer) {
        PayoutSearchResponse response = new PayoutSearchResponse();
        response.setId(transfer.getId());
        response.setReference(transfer.getReference());
        response.setTransferReference(transfer.getReference());
        response.setStatus(transfer.getStatus());
        response.setSenderName(transfer.getSender().getFirstName() + " " + transfer.getSender().getLastName());
        response.setBeneficiaryName(transfer.getBeneficiary().getFirstName() + " " + transfer.getBeneficiary().getLastName());
        response.setBeneficiaryPhoneNumber(transfer.getBeneficiary().getPhoneNumber());
        response.setSentAmount(transfer.getSentAmount());
        response.setReceivedAmount(transfer.getReceivedAmount());
        response.setSourceCurrency(transfer.getSourceCurrency().getCode());
        response.setTargetCurrency(transfer.getTargetCurrency().getCode());
        response.setCreatedAt(transfer.getCreatedAt());
        response.setExpiresAt(transfer.getExpiresAt());
        response.setPaidAt(transfer.getPaidAt());
        return response;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String maskIdentity(String identityNumber) {
        String value = normalize(identityNumber);
        if (value == null) {
            return null;
        }
        if (value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }
}
