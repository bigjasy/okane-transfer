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
import ma.ensam.okanetransfer.repository.UserRepository;

import java.time.LocalDateTime;

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

        return response;
    }
}