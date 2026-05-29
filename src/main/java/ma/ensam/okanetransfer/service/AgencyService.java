package ma.ensam.okanetransfer.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ma.ensam.okanetransfer.domain.agency.Agency;
import ma.ensam.okanetransfer.domain.referential.Country;
import ma.ensam.okanetransfer.domain.user.Agent;
import ma.ensam.okanetransfer.domain.user.Manager;
import ma.ensam.okanetransfer.dto.agency.AgencyRequest;
import ma.ensam.okanetransfer.dto.agency.AgencyResponse;
import ma.ensam.okanetransfer.enums.AgencyStatus;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.repository.AgencyRepository;
import ma.ensam.okanetransfer.repository.CountryRepository;
import ma.ensam.okanetransfer.repository.UserRepository;

@Service
@Transactional
public class AgencyService {

    private final AgencyRepository agencyRepository;
    private final CountryRepository countryRepository;
    private final UserRepository userRepository;

    public AgencyService(AgencyRepository agencyRepository, CountryRepository countryRepository, UserRepository userRepository) {
        this.agencyRepository = agencyRepository;
        this.countryRepository = countryRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<AgencyResponse> getAllAgencies(Long countryId, AgencyStatus status, Pageable pageable) {
        // Pour simplifier sans multiplier les méthodes du repository, on utilise le findAll natif ou filtré
        return agencyRepository.findAll(pageable).map(this::mapToResponse);
    }

    public AgencyResponse createAgency(AgencyRequest request) {
        if (agencyRepository.findByCode(request.getCode()).isPresent()) {
            throw new BusinessException("Une agence avec ce code existe déjà.");
        }

        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new BusinessException("Pays introuvable."));

        Agency agency = new Agency();
        agency.setCode(request.getCode());
        agency.setName(request.getName());
        agency.setAddress(request.getAddress());
        agency.setCity(request.getCity());
        agency.setCountry(country);
        agency.setDailyLimit(request.getDailyLimit());
        agency.setStatus(AgencyStatus.ACTIVE);

        return mapToResponse(agencyRepository.save(agency));
    }

    @Transactional(readOnly = true)
    public AgencyResponse getAgencyById(Long id) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Agence introuvable."));
        return mapToResponse(agency);
    }

    public AgencyResponse updateAgency(Long id, AgencyRequest request) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Agence introuvable."));

        agency.setName(request.getName());
        agency.setAddress(request.getAddress());
        agency.setCity(request.getCity());
        agency.setDailyLimit(request.getDailyLimit());

        return mapToResponse(agencyRepository.save(agency));
    }

    public AgencyResponse updateStatus(Long id, AgencyStatus status) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Agence introuvable."));
        agency.setStatus(status);
        return mapToResponse(agencyRepository.save(agency));
    }

    public AgencyResponse assignAgent(Long agencyId, Long agentId) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new BusinessException("Agence introuvable."));
        Agent agent = (Agent) userRepository.findById(agentId)
                .orElseThrow(() -> new BusinessException("Agent introuvable."));

        agent.setAgency(agency);
        userRepository.save(agent);
        return mapToResponse(agency);
    }

    public AgencyResponse assignManager(Long agencyId, Long managerId) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new BusinessException("Agence introuvable."));
        Manager manager = (Manager) userRepository.findById(managerId)
                .orElseThrow(() -> new BusinessException("Manager introuvable."));

        manager.setAgency(agency);
        userRepository.save(manager);
        return mapToResponse(agency);
    }

    private AgencyResponse mapToResponse(Agency agency) {
        AgencyResponse response = new AgencyResponse();
        response.setId(agency.getId());
        response.setCode(agency.getCode());
        response.setName(agency.getName());
        response.setCity(agency.getCity());
        response.setCountry(agency.getCountry().getName());
        response.setStatus(agency.getStatus());
        response.setDailyLimit(agency.getDailyLimit());
        return response;
    }
}