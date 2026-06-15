package ma.ensam.okanetransfer.service;

import java.util.List;
import ma.ensam.okanetransfer.domain.compliance.WatchlistEntry;
import ma.ensam.okanetransfer.domain.referential.Country;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.repository.WatchlistEntryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BeneficiaryScreeningService {

    private final WatchlistEntryRepository watchlistEntryRepository;

    public BeneficiaryScreeningService(WatchlistEntryRepository watchlistEntryRepository) {
        this.watchlistEntryRepository = watchlistEntryRepository;
    }

    public void assertBeneficiaryNotOnWatchlist(String firstName, String lastName, Country country) {
        List<WatchlistEntry> matches = watchlistEntryRepository.findByLastNameIgnoreCaseAndActiveTrue(lastName.trim());
        for (WatchlistEntry entry : matches) {
            if (namesMatch(entry.getFirstName(), firstName) && countriesMatch(entry, country)) {
                throw new BusinessException(
                        "WATCHLIST_MATCH",
                        "Beneficiary matches an active sanctions watchlist entry (OFAC simulated).",
                        HttpStatus.UNPROCESSABLE_ENTITY
                );
            }
        }
    }

    private boolean namesMatch(String watchlistFirstName, String beneficiaryFirstName) {
        return watchlistFirstName != null
                && beneficiaryFirstName != null
                && watchlistFirstName.equalsIgnoreCase(beneficiaryFirstName.trim());
    }

    private boolean countriesMatch(WatchlistEntry entry, Country beneficiaryCountry) {
        if (entry.getCountry() == null || beneficiaryCountry == null) {
            return true;
        }
        return entry.getCountry().getId().equals(beneficiaryCountry.getId());
    }
}
