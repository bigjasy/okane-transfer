package ma.ensam.okanetransfer.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import ma.ensam.okanetransfer.domain.agency.Agency;
import ma.ensam.okanetransfer.domain.agency.Corridor;
import ma.ensam.okanetransfer.domain.referential.Country;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransferLimitServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @InjectMocks
    private TransferLimitService transferLimitService;

    private Agency agency;
    private Corridor corridor;

    @BeforeEach
    void setUp() {
        agency = new Agency();
        agency.setId(1L);
        agency.setDailyLimit(new BigDecimal("1000"));

        Country source = new Country();
        source.setId(10L);
        Country destination = new Country();
        destination.setId(20L);

        corridor = new Corridor();
        corridor.setSourceCountry(source);
        corridor.setDestinationCountry(destination);
        corridor.setDailyLimit(new BigDecimal("500"));
        corridor.setMonthlyLimit(new BigDecimal("5000"));
    }

    @Test
    void shouldAllowTransferWithinLimits() {
        when(transferRepository.sumSentAmountBySourceAgencyBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("200"));
        when(transferRepository.sumSentAmountByCorridorCountriesBetween(
                eq(10L), eq(20L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("100"));

        assertDoesNotThrow(() -> transferLimitService.validateTransferLimits(
                agency, corridor, new BigDecimal("100")));
    }

    @Test
    void shouldRejectWhenAgencyDailyLimitExceeded() {
        when(transferRepository.sumSentAmountBySourceAgencyBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("950"));

        assertThrows(BusinessException.class, () -> transferLimitService.validateTransferLimits(
                agency, corridor, new BigDecimal("100")));
    }

    @Test
    void shouldRejectWhenCorridorMonthlyLimitExceeded() {
        when(transferRepository.sumSentAmountBySourceAgencyBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transferRepository.sumSentAmountByCorridorCountriesBetween(
                eq(10L), eq(20L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("4950"));

        assertThrows(BusinessException.class, () -> transferLimitService.validateTransferLimits(
                agency, corridor, new BigDecimal("100")));
    }
}
