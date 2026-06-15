package ma.ensam.okanetransfer.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ma.ensam.okanetransfer.domain.agency.Agency;
import ma.ensam.okanetransfer.domain.referential.Country;
import ma.ensam.okanetransfer.domain.user.Agent;
import ma.ensam.okanetransfer.domain.user.Manager;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.dto.agency.AgencyRequest;
import ma.ensam.okanetransfer.dto.agency.AgencyResponse;
import ma.ensam.okanetransfer.dto.agency.AgencyStaffResponse;
import ma.ensam.okanetransfer.dto.user.UserSummaryResponse;
import ma.ensam.okanetransfer.enums.AgencyStatus;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.repository.AgencyRepository;
import ma.ensam.okanetransfer.repository.AgentRepository;
import ma.ensam.okanetransfer.repository.CountryRepository;
import ma.ensam.okanetransfer.repository.ManagerRepository;
import ma.ensam.okanetransfer.repository.UserRepository;

import java.util.List;

@Service
@Transactional
public class AgencyService {

    private final AgencyRepository agencyRepository;
    private final CountryRepository countryRepository;
    private final UserRepository userRepository;
    private final AgentRepository agentRepository;
    private final ManagerRepository managerRepository;

    public AgencyService(
            AgencyRepository agencyRepository,
            CountryRepository countryRepository,
            UserRepository userRepository,
            AgentRepository agentRepository,
            ManagerRepository managerRepository
    ) {
        this.agencyRepository = agencyRepository;
        this.countryRepository = countryRepository;
        this.userRepository = userRepository;
        this.agentRepository = agentRepository;
        this.managerRepository = managerRepository;
    }

    @Transactional(readOnly = true)
    public Page<AgencyResponse> getAllAgencies(Long countryId, AgencyStatus status, Pageable pageable) {
        Page<Agency> agencies;
        if (countryId != null && status != null) {
            agencies = agencyRepository.findByCountryIdAndStatus(countryId, status, pageable);
        } else if (countryId != null) {
            agencies = agencyRepository.findByCountryId(countryId, pageable);
        } else if (status != null) {
            agencies = agencyRepository.findByStatus(status, pageable);
        } else {
            agencies = agencyRepository.findAll(pageable);
        }
        return agencies.map(this::mapToResponse);
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
        User user = userRepository.findById(agentId)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable."));
                
        if (!(user instanceof Agent)) {
            throw new BusinessException("L'utilisateur spécifié n'est pas un Agent.");
        }

        Agent agent = (Agent) user;
        agent.setAgency(agency); 
        
        userRepository.save(agent);
        return mapToResponse(agency);
    }

    public AgencyResponse assignManager(Long agencyId, Long managerId) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new BusinessException("Agence introuvable."));
        
        User user = userRepository.findById(managerId)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable."));

        if (!(user instanceof Manager)) {
            throw new BusinessException("L'utilisateur spécifié n'est pas un Manager.");
        }

        Manager manager = (Manager) user;
        manager.setAgency(agency); 
        
        userRepository.save(manager);
        return mapToResponse(agency);
    }

    @Transactional(readOnly = true)
    public AgencyStaffResponse getAgencyStaff(Long agencyId) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new BusinessException("Agence introuvable."));

        AgencyStaffResponse response = new AgencyStaffResponse();
        response.setAgencyId(agency.getId());
        response.setAgencyCode(agency.getCode());
        response.setAgencyName(agency.getName());
        response.setAgents(agentRepository.findByAgencyId(agencyId).stream()
                .map(UserSummaryResponse::from)
                .toList());
        response.setManagers(managerRepository.findByAgencyId(agencyId).stream()
                .map(UserSummaryResponse::from)
                .toList());
        return response;
    }

    private AgencyResponse mapToResponse(Agency agency) {
        AgencyResponse response = new AgencyResponse();
        response.setId(agency.getId());
        response.setCode(agency.getCode());
        response.setName(agency.getName());
        response.setCity(agency.getCity());
        response.setAddress(agency.getAddress());
        response.setCountry(agency.getCountry().getName());
        response.setStatus(agency.getStatus());
        response.setDailyLimit(agency.getDailyLimit());
        return response;
    }
}