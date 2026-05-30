package ma.ensam.okanetransfer.service;

import ma.ensam.okanetransfer.domain.compliance.WatchlistEntry;
import ma.ensam.okanetransfer.domain.referential.Country;
import ma.ensam.okanetransfer.dto.compliance.WatchlistEntryRequest;
import ma.ensam.okanetransfer.dto.compliance.WatchlistEntryResponse;
import ma.ensam.okanetransfer.dto.common.PageResponse;
import ma.ensam.okanetransfer.exception.ResourceNotFoundException;
import ma.ensam.okanetransfer.repository.CountryRepository;
import ma.ensam.okanetransfer.repository.WatchlistEntryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WatchlistService {

    private final WatchlistEntryRepository watchlistEntryRepository;
    private final CountryRepository countryRepository;

    public WatchlistService(WatchlistEntryRepository watchlistEntryRepository, CountryRepository countryRepository) {
        this.watchlistEntryRepository = watchlistEntryRepository;
        this.countryRepository = countryRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<WatchlistEntryResponse> listEntries(Boolean active, Pageable pageable) {
        Page<WatchlistEntry> page = Boolean.TRUE.equals(active)
                ? watchlistEntryRepository.findByActiveTrue(pageable)
                : watchlistEntryRepository.findAll(pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    public WatchlistEntryResponse createEntry(WatchlistEntryRequest request) {
        WatchlistEntry entry = mapRequest(new WatchlistEntry(), request);
        return toResponse(watchlistEntryRepository.save(entry));
    }

    public WatchlistEntryResponse updateEntry(Long id, WatchlistEntryRequest request) {
        WatchlistEntry entry = watchlistEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WatchlistEntry", id));
        return toResponse(watchlistEntryRepository.save(mapRequest(entry, request)));
    }

    public void deactivateEntry(Long id) {
        WatchlistEntry entry = watchlistEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WatchlistEntry", id));
        entry.setActive(false);
        watchlistEntryRepository.save(entry);
    }

    private WatchlistEntry mapRequest(WatchlistEntry entry, WatchlistEntryRequest request) {
        entry.setFirstName(request.getFirstName());
        entry.setLastName(request.getLastName());
        entry.setSource(request.getSource());
        entry.setActive(request.isActive());
        if (request.getCountryId() != null) {
            Country country = countryRepository.findById(request.getCountryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Country", request.getCountryId()));
            entry.setCountry(country);
        } else {
            entry.setCountry(null);
        }
        return entry;
    }

    private WatchlistEntryResponse toResponse(WatchlistEntry entry) {
        WatchlistEntryResponse response = new WatchlistEntryResponse();
        response.setId(entry.getId());
        response.setFirstName(entry.getFirstName());
        response.setLastName(entry.getLastName());
        response.setSource(entry.getSource());
        response.setActive(entry.isActive());
        response.setCreatedAt(entry.getCreatedAt());
        if (entry.getCountry() != null) {
            response.setCountryName(entry.getCountry().getName());
        }
        return response;
    }
}
