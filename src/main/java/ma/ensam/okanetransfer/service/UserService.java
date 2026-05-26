package ma.ensam.okanetransfer.service;

import jakarta.persistence.criteria.Predicate;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import ma.ensam.okanetransfer.domain.audit.AuditLog;
import ma.ensam.okanetransfer.domain.user.Admin;
import ma.ensam.okanetransfer.domain.user.Agent;
import ma.ensam.okanetransfer.domain.user.Manager;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.dto.audit.AuditLogResponse;
import ma.ensam.okanetransfer.dto.common.PageResponse;
import ma.ensam.okanetransfer.dto.user.UserCreateRequest;
import ma.ensam.okanetransfer.dto.user.UserProfileResponse;
import ma.ensam.okanetransfer.dto.user.UserStatusUpdateRequest;
import ma.ensam.okanetransfer.dto.user.UserSummaryResponse;
import ma.ensam.okanetransfer.dto.user.UserUpdateRequest;
import ma.ensam.okanetransfer.enums.AuditAction;
import ma.ensam.okanetransfer.enums.Role;
import ma.ensam.okanetransfer.enums.UserStatus;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.exception.ForbiddenOperationException;
import ma.ensam.okanetransfer.exception.ResourceNotFoundException;
import ma.ensam.okanetransfer.repository.AdminRepository;
import ma.ensam.okanetransfer.repository.AuditLogRepository;
import ma.ensam.okanetransfer.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(
            UserRepository userRepository,
            AdminRepository adminRepository,
            AuditLogRepository auditLogRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserSummaryResponse> listUsers(
            Role role,
            UserStatus status,
            Long agencyId,
            Pageable pageable,
            User actor
    ) {
        requireAny(actor, Role.ROLE_ADMIN, Role.ROLE_MANAGER);
        Long effectiveAgencyId = actor instanceof Manager manager ? manager.getAgencyId() : agencyId;
        List<User> filtered = userRepository.findAll(withFilters(role, status))
                .stream()
                .filter(user -> matchesAgency(user, effectiveAgencyId))
                .toList();
        int start = Math.min((int) pageable.getOffset(), filtered.size());
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<UserSummaryResponse> content = filtered.subList(start, end)
                .stream()
                .map(UserSummaryResponse::from)
                .toList();
        int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / pageable.getPageSize());
        return new PageResponse<>(content, pageable.getPageNumber(), pageable.getPageSize(), filtered.size(), totalPages);
    }

    @Transactional
    public UserSummaryResponse createInternalUser(
            UserCreateRequest request,
            User actor,
            String ipAddress,
            String userAgent
    ) {
        requireAny(actor, Role.ROLE_ADMIN, Role.ROLE_MANAGER);
        if (request.role() == Role.ROLE_CLIENT) {
            throw new BusinessException("INVALID_INTERNAL_ROLE", "Use register-client for client accounts", HttpStatus.BAD_REQUEST);
        }
        if (actor.getRole() == Role.ROLE_MANAGER && request.role() != Role.ROLE_AGENT) {
            throw new ForbiddenOperationException("Managers can create agents only");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("EMAIL_ALREADY_EXISTS", "Email is already used", HttpStatus.CONFLICT);
        }

        User user = newUserForRole(request.role());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setPasswordHash(passwordEncoder.encode(generateTemporaryPassword()));
        user.setStatus(UserStatus.ACTIVE);
        assignAgency(user, request.agencyId());

        User saved = userRepository.save(user);
        auditService.record(AuditAction.USER_CREATED, actor, "User", String.valueOf(saved.getId()), ipAddress, userAgent, null);
        return UserSummaryResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUser(Long id, User actor) {
        User user = getUserEntity(id);
        if (!canRead(actor, user)) {
            throw new ForbiddenOperationException("You cannot access this user");
        }
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserProfileResponse updateUser(Long id, UserUpdateRequest request, User actor) {
        User user = getUserEntity(id);
        if (!(isAdmin(actor) || isOwner(actor, user))) {
            throw new ForbiddenOperationException("You cannot update this user");
        }
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhoneNumber(request.phoneNumber());
        if (request.preferredLanguage() != null) {
            user.setPreferredLanguage(request.preferredLanguage());
        }
        return UserProfileResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserSummaryResponse updateStatus(
            Long id,
            UserStatusUpdateRequest request,
            User actor,
            String ipAddress,
            String userAgent
    ) {
        requireAny(actor, Role.ROLE_ADMIN);
        User user = getUserEntity(id);
        user.setStatus(request.status());
        User saved = userRepository.save(user);
        auditService.record(
                AuditAction.USER_SUSPENDED,
                actor,
                "User",
                String.valueOf(saved.getId()),
                ipAddress,
                userAgent,
                "{\"status\":\"" + request.status() + "\",\"reason\":\"" + nullToEmpty(request.reason()) + "\"}"
        );
        return UserSummaryResponse.from(saved);
    }

    @Transactional
    public UserSummaryResponse updateRole(Long id, Role role, User actor) {
        requireAny(actor, Role.ROLE_ADMIN);
        User user = getUserEntity(id);
        if (user instanceof Admin admin && admin.isSuperAdmin() && role != Role.ROLE_ADMIN && adminRepository.findBySuperAdminTrue().size() <= 1) {
            throw new ForbiddenOperationException("Cannot remove the last super admin role");
        }
        if (role == Role.ROLE_CLIENT) {
            throw new BusinessException("INVALID_ROLE_CHANGE", "Client role changes require client account migration", HttpStatus.BAD_REQUEST);
        }
        user.assignRole(role);
        return UserSummaryResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> auditLogs(Long id, User actor) {
        requireAny(actor, Role.ROLE_ADMIN);
        getUserEntity(id);
        List<AuditLog> auditLogs = auditLogRepository.findByActorUserId(id);
        return auditLogs.stream().map(AuditLogResponse::from).toList();
    }

    private Specification<User> withFilters(Role role, UserStatus status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (role != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private User getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private boolean canRead(User actor, User target) {
        return isAdmin(actor) || isOwner(actor, target) || (actor.getRole() == Role.ROLE_MANAGER && sameAgency(actor, target));
    }

    private boolean isAdmin(User user) {
        return user.getRole() == Role.ROLE_ADMIN;
    }

    private boolean isOwner(User actor, User target) {
        return actor.getId() != null && actor.getId().equals(target.getId());
    }

    private boolean sameAgency(User left, User right) {
        Long leftAgencyId = agencyId(left);
        Long rightAgencyId = agencyId(right);
        return leftAgencyId != null && leftAgencyId.equals(rightAgencyId);
    }

    private boolean matchesAgency(User user, Long agencyId) {
        return agencyId == null || agencyId.equals(agencyId(user));
    }

    private Long agencyId(User user) {
        if (user instanceof Manager manager) {
            return manager.getAgencyId();
        }
        if (user instanceof Agent agent) {
            return agent.getAgencyId();
        }
        return null;
    }

    private void assignAgency(User user, Long agencyId) {
        if (user instanceof Manager manager) {
            manager.setAgencyId(agencyId);
        }
        if (user instanceof Agent agent) {
            agent.setAgencyId(agencyId);
        }
    }

    private User newUserForRole(Role role) {
        return switch (role) {
            case ROLE_ADMIN -> new Admin();
            case ROLE_MANAGER -> new Manager();
            case ROLE_AGENT -> new Agent();
            case ROLE_CLIENT -> throw new BusinessException("INVALID_INTERNAL_ROLE", "Invalid internal role", HttpStatus.BAD_REQUEST);
        };
    }

    private void requireAny(User actor, Role... roles) {
        for (Role role : roles) {
            if (actor.getRole() == role) {
                return;
            }
        }
        throw new ForbiddenOperationException("Insufficient permissions");
    }

    private String generateTemporaryPassword() {
        byte[] bytes = new byte[24];
        SECURE_RANDOM.nextBytes(bytes);
        return "Tmp@" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes) + "9aA";
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
