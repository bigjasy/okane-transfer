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
import ma.ensam.okanetransfer.dto.transfer.PayoutResponse;
import ma.ensam.okanetransfer.enums.CashMovementType;
import ma.ensam.okanetransfer.enums.CashRegisterStatus;
import ma.ensam.okanetransfer.enums.TransferStatus;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.repository.CashMovementRepository;
import ma.ensam.okanetransfer.repository.CashRegisterRepository;
import ma.ensam.okanetransfer.repository.TransferPaymentRepository;
import ma.ensam.okanetransfer.repository.TransferRepository;

import java.time.LocalDateTime;

@Service
@Transactional
public class PayoutService {

    private final TransferRepository transferRepository;
    private final TransferPaymentRepository transferPaymentRepository;
    private final CashRegisterRepository cashRegisterRepository;
    private final CashMovementRepository cashMovementRepository;
    private final PasswordEncoder passwordEncoder;

    public PayoutService(TransferRepository transferRepository, 
                         TransferPaymentRepository transferPaymentRepository,
                         CashRegisterRepository cashRegisterRepository, 
                         CashMovementRepository cashMovementRepository,
                         PasswordEncoder passwordEncoder) {
        this.transferRepository = transferRepository;
        this.transferPaymentRepository = transferPaymentRepository;
        this.cashRegisterRepository = cashRegisterRepository;
        this.cashMovementRepository = cashMovementRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public PayoutResponse confirmPayout(PayoutConfirmRequest request, Long agentId) {
        // 1. Récupération et vérification de l'état du transfert
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

        // 2. Vérification sécurisée du code de retrait (Le hash en base vs le code en clair saisi)
        if (!passwordEncoder.matches(request.getWithdrawalCode(), transfer.getWithdrawalCodeHash())) {
            throw new BusinessException("Code de retrait invalide.");
        }

        // 3. Vérification de la caisse de l'agent
        CashRegister register = cashRegisterRepository.findByAgentIdAndStatus(agentId, CashRegisterStatus.OPEN)
                .orElseThrow(() -> new BusinessException("Vous devez avoir une caisse ouverte pour effectuer un paiement."));

        // Vérification de la devise de la caisse vs la devise cible du transfert
        if (!register.getCurrency().getCode().equals(transfer.getTargetCurrency().getCode())) {
            throw new BusinessException("La devise de votre caisse ne correspond pas à la devise de réception du transfert.");
        }

        // Vérification de la liquidité (Fonds suffisants ?)
        if (register.getCurrentBalance().compareTo(transfer.getReceivedAmount()) < 0) {
            throw new BusinessException("Fonds insuffisants dans la caisse pour payer ce transfert.");
        }

        // 4. Exécution du paiement
        transfer.setStatus(TransferStatus.PAID);
        transfer.setPaidAt(LocalDateTime.now());
        transferRepository.save(transfer);

        // Mappage de l'agent factice
        Agent agent = new Agent();
        agent.setId(agentId);

        // 5. Création de la trace de paiement (TransferPayment)
        TransferPayment payment = new TransferPayment();
        payment.setTransfer(transfer);
        payment.setPaidByAgent(agent);
        payment.setPaidAtAgency(register.getAgency());
        payment.setBeneficiaryIdentityType(request.getIdentityType());
        
        // Grâce au @Convert(converter = AesEncryptionConverter.class) dans l'entité,
        // JPA va automatiquement chiffrer cette valeur en base. On lui passe donc la valeur en clair.
        payment.setBeneficiaryIdentityNumberEncrypted(request.getIdentityNumber());
        payment.setPaidAmount(transfer.getReceivedAmount());
        
        transferPaymentRepository.save(payment);

        // 6. Impact sur la caisse : Mouvement CASH_OUT
        CashMovement movement = new CashMovement();
        movement.setCashRegister(register);
        movement.setType(CashMovementType.CASH_OUT);
        movement.setAmount(transfer.getReceivedAmount());
        movement.setCurrency(transfer.getTargetCurrency());
        movement.setTransfer(transfer);
        movement.setReason("Paiement du transfert " + transfer.getReference());
        movement.setCreatedBy(agent);

        cashMovementRepository.save(movement);

        // Mise à jour du solde
        register.setCurrentBalance(register.getCurrentBalance().subtract(transfer.getReceivedAmount()));
        cashRegisterRepository.save(register);

        // 7. Retour de la réponse au Frontend
        PayoutResponse response = new PayoutResponse();
        response.setTransferReference(transfer.getReference());
        response.setStatus(transfer.getStatus());
        response.setPaidAmount(payment.getPaidAmount());
        response.setCurrency(transfer.getTargetCurrency().getCode());
        response.setPaidAt(transfer.getPaidAt());

        return response;
    }
}