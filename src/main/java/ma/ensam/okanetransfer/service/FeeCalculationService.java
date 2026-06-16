package ma.ensam.okanetransfer.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ma.ensam.okanetransfer.domain.agency.FeeGrid;
import ma.ensam.okanetransfer.domain.referential.ExchangeRate;
import ma.ensam.okanetransfer.dto.agency.FeeSimulationRequest;
import ma.ensam.okanetransfer.dto.agency.FeeSimulationResponse;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.repository.ExchangeRateRepository;
import ma.ensam.okanetransfer.repository.FeeGridRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@Transactional(readOnly = true) // readOnly car ce service ne fait que consulter la BDD pour faire des maths
public class FeeCalculationService {

    private final FeeGridRepository feeGridRepository;
    private final ExchangeRateRepository exchangeRateRepository;

    public FeeCalculationService(FeeGridRepository feeGridRepository, ExchangeRateRepository exchangeRateRepository) {
        this.feeGridRepository = feeGridRepository;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public FeeSimulationResponse simulateFees(FeeSimulationRequest request) {
        BigDecimal amount = request.getAmount();

        // 1. Récupération de la grille tarifaire active
        FeeGrid grid;
        if (request.getCorridorId() != null) {
            grid = feeGridRepository.findActiveGrid(request.getCorridorId(), amount, LocalDate.now())
                    .orElseThrow(() -> new BusinessException("Aucune grille tarifaire active trouvée pour ce montant et ce corridor."));
        } else {
            grid = feeGridRepository.findActiveGridByCurrencies(
                    request.getSourceCurrency(), request.getTargetCurrency(), amount, LocalDate.now())
                    .orElseThrow(() -> new BusinessException("Aucune grille tarifaire active trouvée pour ce montant et ces devises."));
        }

        // 2. Récupération du taux de change actif
        // On suppose que M3 a créé une méthode findActiveRate dans ExchangeRateRepository
        ExchangeRate exchangeRate = exchangeRateRepository.findActiveRate(request.getSourceCurrency(), request.getTargetCurrency())
                .orElseThrow(() -> new BusinessException("Aucun taux de change actif trouvé pour ces devises."));

        // 3. Application des formules mathématiques
        BigDecimal rateValue = exchangeRate.getRate();

        // Frais = fixedFee + (amount * percentageFee / 100)
        BigDecimal percentageFeeAmount = amount.multiply(grid.getPercentageFee())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal feeAmount = grid.getFixedFee().add(percentageFeeAmount);

        // Total à payer = amount + feeAmount
        BigDecimal totalToPay = amount.add(feeAmount);

        // Montant reçu = amount * exchangeRateApplied
        BigDecimal receivedAmount = amount.multiply(rateValue)
                .setScale(2, RoundingMode.HALF_UP);

        // Commission agence = feeAmount * agencyCommissionRate / 100
        BigDecimal agencyCommission = feeAmount.multiply(grid.getAgencyCommissionRate())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        // Commission centrale = feeAmount * centralCommissionRate / 100
        BigDecimal centralCommission = feeAmount.multiply(grid.getCentralCommissionRate())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        // 4. Construction de la réponse (DTO)
        FeeSimulationResponse response = new FeeSimulationResponse();
        response.setAmount(amount);
        response.setFeeAmount(feeAmount);
        response.setTotalToPay(totalToPay);
        response.setExchangeRate(rateValue);
        response.setReceivedAmount(receivedAmount);
        response.setAgencyCommission(agencyCommission);
        response.setCentralCommission(centralCommission);

        return response;
    }
}