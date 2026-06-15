package ma.ensam.okanetransfer.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ma.ensam.okanetransfer.domain.referential.Country;
import ma.ensam.okanetransfer.domain.transfer.Beneficiary;
import ma.ensam.okanetransfer.domain.user.Client;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.dto.transfer.BeneficiaryRequest;
import ma.ensam.okanetransfer.dto.transfer.BeneficiaryResponse;
import ma.ensam.okanetransfer.enums.Role;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.repository.BeneficiaryRepository;
import ma.ensam.okanetransfer.repository.CountryRepository;
import ma.ensam.okanetransfer.repository.UserRepository;

@Service
@Transactional
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;
    private final CountryRepository countryRepository;
    private final BeneficiaryScreeningService beneficiaryScreeningService;

    public BeneficiaryService(BeneficiaryRepository beneficiaryRepository,
                              UserRepository userRepository,
                              CountryRepository countryRepository,
                              BeneficiaryScreeningService beneficiaryScreeningService) {
        this.beneficiaryRepository = beneficiaryRepository;
        this.userRepository = userRepository;
        this.countryRepository = countryRepository;
        this.beneficiaryScreeningService = beneficiaryScreeningService;
    }

    @Transactional(readOnly = true)
    public Page<BeneficiaryResponse> getBeneficiaries(Long clientId, String authenticatedEmail, Pageable pageable) {
        User user = userRepository.findByEmailIgnoreCase(authenticatedEmail)
                .orElseThrow(() -> new BusinessException("Utilisateur connecté introuvable."));

        // Règle de cloisonnement : Un client ne peut lister que ses propres bénéficiaires
        if (user.getRole() == Role.ROLE_CLIENT) {
            clientId = user.getId();
        }

        Page<Beneficiary> beneficiaries;
        if (clientId != null) {
            beneficiaries = beneficiaryRepository.findByClientId(clientId, pageable);
        } else {
            beneficiaries = beneficiaryRepository.findAll(pageable);
        }

        return beneficiaries.map(this::mapToResponse);
    }

    public BeneficiaryResponse createBeneficiary(BeneficiaryRequest request, String authenticatedEmail) {
        User user = userRepository.findByEmailIgnoreCase(authenticatedEmail)
                .orElseThrow(() -> new BusinessException("Utilisateur connecté introuvable."));

        Client clientSender;
        if (user.getRole() == Role.ROLE_CLIENT) {
            clientSender = (Client) user;
        } else {
            throw new BusinessException("La création isolée de bénéficiaire nécessite un espace client ou un contexte d'envoi défini.");
        }

        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new BusinessException("Pays spécifié introuvable."));

        beneficiaryScreeningService.assertBeneficiaryNotOnWatchlist(
                request.getFirstName(),
                request.getLastName(),
                country
        );

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setClient(clientSender);
        beneficiary.setFirstName(request.getFirstName().trim());
        beneficiary.setLastName(request.getLastName().trim());
        beneficiary.setPhoneNumber(request.getPhoneNumber().trim());
        beneficiary.setCountry(country);
        beneficiary.setIdentityType(request.getIdentityType());
        // Affectation transparente. Le chiffrement s'exécute au niveau JPA via le convertisseur global
        beneficiary.setIdentityNumberEncrypted(request.getIdentityNumber().trim());

        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public BeneficiaryResponse getBeneficiaryById(Long id, String authenticatedEmail) {
        Beneficiary beneficiary = beneficiaryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Bénéficiaire introuvable."));

        User user = userRepository.findByEmailIgnoreCase(authenticatedEmail)
                .orElseThrow(() -> new BusinessException("Utilisateur connecté introuvable."));

        if (user.getRole() == Role.ROLE_CLIENT && !beneficiary.getClient().getId().equals(user.getId())) {
            throw new BusinessException("Accès refusé. Ce bénéficiaire ne vous appartient pas.");
        }

        return mapToResponse(beneficiary);
    }

    public BeneficiaryResponse updateBeneficiary(Long id, BeneficiaryRequest request, String authenticatedEmail) {
        Beneficiary beneficiary = beneficiaryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Bénéficiaire introuvable."));

        User user = userRepository.findByEmailIgnoreCase(authenticatedEmail)
                .orElseThrow(() -> new BusinessException("Utilisateur connecté introuvable."));

        if (user.getRole() == Role.ROLE_CLIENT && !beneficiary.getClient().getId().equals(user.getId())) {
            throw new BusinessException("Modification refusée. Ce bénéficiaire ne vous appartient pas.");
        }

        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new BusinessException("Pays spécifié introuvable."));

        beneficiaryScreeningService.assertBeneficiaryNotOnWatchlist(
                request.getFirstName(),
                request.getLastName(),
                country
        );

        beneficiary.setFirstName(request.getFirstName().trim());
        beneficiary.setLastName(request.getLastName().trim());
        beneficiary.setPhoneNumber(request.getPhoneNumber().trim());
        beneficiary.setCountry(country);
        beneficiary.setIdentityType(request.getIdentityType());
        beneficiary.setIdentityNumberEncrypted(request.getIdentityNumber().trim());

        Beneficiary updated = beneficiaryRepository.save(beneficiary);
        return mapToResponse(updated);
    }

    public void deleteBeneficiary(Long id, String authenticatedEmail) {
        Beneficiary beneficiary = beneficiaryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Bénéficiaire introuvable."));

        User user = userRepository.findByEmailIgnoreCase(authenticatedEmail)
                .orElseThrow(() -> new BusinessException("Utilisateur connecté introuvable."));

        if (user.getRole() != Role.ROLE_CLIENT) {
            throw new BusinessException("L'action de suppression ou désactivation est réservée au propriétaire client.");
        }

        if (!beneficiary.getClient().getId().equals(user.getId())) {
            throw new BusinessException("Suppression refusée. Ce bénéficiaire ne vous appartient pas.");
        }

        beneficiaryRepository.delete(beneficiary);
    }

    private BeneficiaryResponse mapToResponse(Beneficiary beneficiary) {
        BeneficiaryResponse response = new BeneficiaryResponse();
        response.setId(beneficiary.getId());
        response.setFullName(beneficiary.getFirstName() + " " + beneficiary.getLastName());
        response.setPhoneNumber(beneficiary.getPhoneNumber());
        response.setCountry(beneficiary.getCountry().getName());
        return response;
    }
}