package ma.ensam.okanetransfer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import ma.ensam.okanetransfer.domain.agency.Agency;
import ma.ensam.okanetransfer.domain.finance.Commission;
import ma.ensam.okanetransfer.domain.referential.Currency;
import ma.ensam.okanetransfer.domain.transfer.Transfer;
import ma.ensam.okanetransfer.domain.user.Admin;
import ma.ensam.okanetransfer.domain.user.Manager;
import ma.ensam.okanetransfer.exception.ForbiddenOperationException;
import ma.ensam.okanetransfer.repository.CommissionRepository;
import ma.ensam.okanetransfer.repository.TransferRepository;
import ma.ensam.okanetransfer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class CommissionServiceTest {

    @Mock
    private CommissionRepository commissionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransferRepository transferRepository;

    @InjectMocks
    private CommissionService commissionService;

    private Admin admin;
    private Manager manager;
    private Agency agency;
    private Transfer transfer;

    @BeforeEach
    void setUp() {
        agency = new Agency();
        agency.setId(1L);
        agency.setName("Okane Casablanca Centre");

        admin = new Admin();
        admin.setEmail("admin@okane.ma");

        manager = new Manager();
        manager.setEmail("manager@okane.ma");
        manager.setAgency(agency);

        transfer = new Transfer();
        transfer.setId(99L);
        transfer.setReference("OKN-TEST-001");
        transfer.setSourceAgency(agency);
    }

    @Test
    void adminShouldListAllCommissionsWhenNoAgencyFilter() {
        Commission commission = sampleCommission();

        when(userRepository.findByEmailIgnoreCase("admin@okane.ma")).thenReturn(Optional.of(admin));
        when(commissionRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(commission)));

        var result = commissionService.listCommissions("admin@okane.ma", null, PageRequest.of(0, 20));

        assertEquals(1, result.content().size());
    }

    @Test
    void managerShouldBeScopedToOwnAgency() {
        when(userRepository.findByEmailIgnoreCase("manager@okane.ma")).thenReturn(Optional.of(manager));
        when(commissionRepository.findByAgencyIdOrderByCreatedAtDesc(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of()));

        commissionService.listCommissions("manager@okane.ma", null, PageRequest.of(0, 20));

        verify(commissionRepository).findByAgencyIdOrderByCreatedAtDesc(eq(1L), any());
    }

    @Test
    void managerShouldNotAccessOtherAgencyCommissions() {
        when(userRepository.findByEmailIgnoreCase("manager@okane.ma")).thenReturn(Optional.of(manager));

        assertThrows(ForbiddenOperationException.class, () ->
                commissionService.listCommissions("manager@okane.ma", 2L, PageRequest.of(0, 20)));
    }

    @Test
    void managerShouldReadCommissionsForOwnTransfer() {
        when(userRepository.findByEmailIgnoreCase("manager@okane.ma")).thenReturn(Optional.of(manager));
        when(transferRepository.findByReference("OKN-TEST-001")).thenReturn(Optional.of(transfer));
        when(commissionRepository.findByTransferId(99L)).thenReturn(List.of(sampleCommission()));

        var result = commissionService.getCommissionsByTransferReference("manager@okane.ma", "OKN-TEST-001");

        assertEquals(1, result.size());
    }

    private Commission sampleCommission() {
        Commission commission = new Commission();
        commission.setId(1L);
        commission.setTransfer(transfer);
        commission.setAgency(agency);
        Currency currency = new Currency();
        currency.setCode("MAD");
        commission.setCurrency(currency);
        return commission;
    }
}
