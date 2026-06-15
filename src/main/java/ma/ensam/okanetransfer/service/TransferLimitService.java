package ma.ensam.okanetransfer.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import ma.ensam.okanetransfer.domain.agency.Agency;
import ma.ensam.okanetransfer.domain.agency.Corridor;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TransferLimitService {

    private final TransferRepository transferRepository;

    public TransferLimitService(TransferRepository transferRepository) {
        this.transferRepository = transferRepository;
    }

    public void validateTransferLimits(Agency sourceAgency, Corridor corridor, BigDecimal amount) {
        validateAgencyDailyLimit(sourceAgency, amount);
        validateCorridorDailyLimit(corridor, amount);
        validateCorridorMonthlyLimit(corridor, amount);
    }

    private void validateAgencyDailyLimit(Agency agency, BigDecimal amount) {
        if (agency.getDailyLimit() == null) {
            return;
        }
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        BigDecimal used = transferRepository.sumSentAmountBySourceAgencyBetween(
                agency.getId(), startOfDay, endOfDay);
        BigDecimal projected = used.add(amount);
        if (projected.compareTo(agency.getDailyLimit()) > 0) {
            throw new BusinessException(
                    "Plafond journalier de l'agence dépassé. Utilisé: "
                            + used + ", limite: " + agency.getDailyLimit() + ".");
        }
    }

    private void validateCorridorDailyLimit(Corridor corridor, BigDecimal amount) {
        if (corridor.getDailyLimit() == null) {
            return;
        }
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        BigDecimal used = transferRepository.sumSentAmountByCorridorCountriesBetween(
                corridor.getSourceCountry().getId(),
                corridor.getDestinationCountry().getId(),
                startOfDay,
                endOfDay);
        BigDecimal projected = used.add(amount);
        if (projected.compareTo(corridor.getDailyLimit()) > 0) {
            throw new BusinessException(
                    "Plafond journalier du corridor dépassé. Utilisé: "
                            + used + ", limite: " + corridor.getDailyLimit() + ".");
        }
    }

    private void validateCorridorMonthlyLimit(Corridor corridor, BigDecimal amount) {
        if (corridor.getMonthlyLimit() == null) {
            return;
        }
        LocalDate firstDay = LocalDate.now().withDayOfMonth(1);
        LocalDateTime startOfMonth = firstDay.atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(LocalTime.MAX);
        BigDecimal used = transferRepository.sumSentAmountByCorridorCountriesBetween(
                corridor.getSourceCountry().getId(),
                corridor.getDestinationCountry().getId(),
                startOfMonth,
                endOfMonth);
        BigDecimal projected = used.add(amount);
        if (projected.compareTo(corridor.getMonthlyLimit()) > 0) {
            throw new BusinessException(
                    "Plafond mensuel du corridor dépassé. Utilisé: "
                            + used + ", limite: " + corridor.getMonthlyLimit() + ".");
        }
    }
}
