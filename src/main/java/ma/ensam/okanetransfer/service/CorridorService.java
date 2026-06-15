package ma.ensam.okanetransfer.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ma.ensam.okanetransfer.domain.agency.Corridor;
import ma.ensam.okanetransfer.domain.referential.Country;
import ma.ensam.okanetransfer.dto.agency.CorridorRequest;
import ma.ensam.okanetransfer.dto.agency.CorridorResponse;
import ma.ensam.okanetransfer.dto.referential.CountryResponse;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.repository.CorridorRepository;
import ma.ensam.okanetransfer.repository.CountryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CorridorService {

    private final CorridorRepository corridorRepository;
    private final CountryRepository countryRepository;

    public CorridorService(CorridorRepository corridorRepository, CountryRepository countryRepository) {
        this.corridorRepository = corridorRepository;
        this.countryRepository = countryRepository;
    }

    @Transactional(readOnly = true)
    public List<CorridorResponse> getAllCorridors() {
        return corridorRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public CorridorResponse createCorridor(CorridorRequest request) {
        if (corridorRepository.findBySourceCountryIdAndDestinationCountryId(request.getSourceCountryId(), request.getDestinationCountryId()).isPresent()) {
            throw new BusinessException("Ce corridor pays source / destination est déjà configuré.");
        }

        Country source = countryRepository.findById(request.getSourceCountryId()).orElseThrow();
        Country dest = countryRepository.findById(request.getDestinationCountryId()).orElseThrow();

        Corridor corridor = new Corridor();
        corridor.setSourceCountry(source);
        corridor.setDestinationCountry(dest);
        corridor.setDailyLimit(request.getDailyLimit());
        corridor.setMonthlyLimit(request.getMonthlyLimit());
        corridor.setActive(true);

        return mapToResponse(corridorRepository.save(corridor));
    }

    public CorridorResponse updateCorridor(Long id, CorridorRequest request) {
        Corridor corridor = corridorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Corridor introuvable."));
        
        corridor.setDailyLimit(request.getDailyLimit());
        corridor.setMonthlyLimit(request.getMonthlyLimit());
        corridor.setActive(request.isActive());

        return mapToResponse(corridorRepository.save(corridor));
    }

    public CorridorResponse toggleActivation(Long id, boolean active) {
        Corridor corridor = corridorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Corridor introuvable."));
        corridor.setActive(active);
        return mapToResponse(corridorRepository.save(corridor));
    }

    private CorridorResponse mapToResponse(Corridor corridor) {
        CorridorResponse res = new CorridorResponse();
        res.setId(corridor.getId());
        res.setActive(corridor.isActive());
        res.setDailyLimit(corridor.getDailyLimit());
        res.setMonthlyLimit(corridor.getMonthlyLimit());
        
        CountryResponse source = new CountryResponse();
        source.setId(corridor.getSourceCountry().getId());
        source.setIsoCode(corridor.getSourceCountry().getIsoCode());
        source.setName(corridor.getSourceCountry().getName());
        source.setActive(corridor.getSourceCountry().isActive());
        res.setSourceCountry(source);

        CountryResponse dest = new CountryResponse();
        dest.setId(corridor.getDestinationCountry().getId());
        dest.setIsoCode(corridor.getDestinationCountry().getIsoCode());
        dest.setName(corridor.getDestinationCountry().getName());
        dest.setActive(corridor.getDestinationCountry().isActive());
        res.setDestinationCountry(dest);

        return res;
    }
}